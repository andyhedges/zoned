// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.DnsRecordTypeDom;
import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@Getter
@Builder
@ToString
public class CsyncRecordDataDom implements RDataDom {
    private long serial;
    private int flags;
    private List<DnsRecordTypeDom> types;

    public static RDataDom from(byte[] rdata) {
        if (rdata == null || rdata.length <= 6) {
            throw new IllegalArgumentException("CSYNC RDATA requires serial, flags, and a type bitmap");
        }
        long serial = readU32(rdata, 0);
        int flags = RDataUtils.readU16(rdata, 4);
        int idx = 6;
        int previousWindow = -1;
        List<DnsRecordTypeDom> types = new ArrayList<>();
        Set<Integer> seenTypes = new HashSet<>();

        while (idx < rdata.length) {
            if (idx + 2 > rdata.length) {
                throw new IllegalArgumentException("CSYNC window header truncated");
            }
            int window = RDataUtils.readU8(rdata, idx);
            int length = RDataUtils.readU8(rdata, idx + 1);
            idx += 2;

            if (length == 0 || length > 32) {
                throw new IllegalArgumentException("CSYNC window length must be between 1 and 32");
            }
            if (window <= previousWindow) {
                throw new IllegalArgumentException("CSYNC windows must be in strictly increasing order");
            }
            if (idx + length > rdata.length) {
                throw new IllegalArgumentException("CSYNC window length exceeds RDATA bounds");
            }

            boolean windowHasTypes = false;
            for (int i = 0; i < length; i++) {
                int value = rdata[idx + i] & 0xFF;
                if (value == 0) {
                    continue;
                }
                for (int bit = 0; bit < 8; bit++) {
                    if ((value & (0x80 >> bit)) != 0) {
                        int typeCode = (window << 8) | (i * 8 + bit);
                        DnsRecordTypeDom type;
                        try {
                            type = DnsRecordTypeDom.fromCode(typeCode);
                        } catch (UnsupportedOperationException e) {
                            throw new IllegalArgumentException("Unsupported CSYNC type code: " + typeCode, e);
                        }
                        if (!seenTypes.add(typeCode)) {
                            throw new IllegalArgumentException("Duplicate CSYNC type code: " + typeCode);
                        }
                        types.add(type);
                        windowHasTypes = true;
                    }
                }
            }
            if (!windowHasTypes || rdata[idx + length - 1] == 0) {
                throw new IllegalArgumentException("CSYNC window bitmap must not be empty or padded");
            }

            idx += length;
            previousWindow = window;
        }

        if (types.isEmpty()) {
            throw new IllegalArgumentException("CSYNC type bitmap must contain at least one type");
        }

        return CsyncRecordDataDom.builder()
                .serial(serial)
                .flags(flags)
                .types(types)
                .build();
    }

    @Override
    public byte[] to() {
        validateU32(serial, "serial");
        if (flags < 0 || flags > 0xFFFF) {
            throw new IllegalArgumentException("CSYNC flags must be between 0 and 65535");
        }
        if (types == null || types.isEmpty()) {
            throw new IllegalArgumentException("CSYNC type list must not be empty");
        }
        Set<Integer> codes = new HashSet<>();
        Map<Integer, BitSet> windows = new TreeMap<>();
        for (int i = 0; i < types.size(); i++) {
            DnsRecordTypeDom type = types.get(i);
            if (type == null) {
                throw new IllegalArgumentException("CSYNC type[" + i + "] is null");
            }
            int code = type.code();
            if (!codes.add(code)) {
                throw new IllegalArgumentException("Duplicate CSYNC type code: " + code);
            }
            int window = code >> 8;
            int bit = code & 0xFF;
            windows.computeIfAbsent(window, ignored -> new BitSet(256)).set(bit);
        }

        int size = 6;
        for (Map.Entry<Integer, BitSet> entry : windows.entrySet()) {
            BitSet bits = entry.getValue();
            int length = (bits.length() + 7) / 8;
            if (length == 0 || length > 32) {
                throw new IllegalArgumentException("CSYNC window length must be between 1 and 32");
            }
            size += 2 + length;
        }

        byte[] out = new byte[size];
        int idx = 0;
        writeU32(out, idx, serial);
        idx += 4;
        out[idx++] = (byte) ((flags >> 8) & 0xFF);
        out[idx++] = (byte) (flags & 0xFF);

        for (Map.Entry<Integer, BitSet> entry : windows.entrySet()) {
            int window = entry.getKey();
            BitSet bits = entry.getValue();
            int length = (bits.length() + 7) / 8;
            out[idx++] = (byte) (window & 0xFF);
            out[idx++] = (byte) (length & 0xFF);
            byte[] bitmap = new byte[length];
            for (int bit = bits.nextSetBit(0); bit >= 0; bit = bits.nextSetBit(bit + 1)) {
                int byteIndex = bit / 8;
                int bitIndex = bit % 8;
                bitmap[byteIndex] |= (byte) (0x80 >> bitIndex);
            }
            System.arraycopy(bitmap, 0, out, idx, bitmap.length);
            idx += bitmap.length;
        }

        if (idx != out.length) {
            throw new IllegalStateException("Bug: CSYNC RDATA encoded length mismatch");
        }
        return out;
    }

    private static long readU32(byte[] rdata, int offset) {
        if (rdata == null) {
            throw new IllegalArgumentException("RDATA is null");
        }
        if (offset < 0 || offset + 3 >= rdata.length) {
            throw new IllegalArgumentException("RDATA offset out of bounds");
        }
        return ((rdata[offset] & 0xFFL) << 24)
                | ((rdata[offset + 1] & 0xFFL) << 16)
                | ((rdata[offset + 2] & 0xFFL) << 8)
                | (rdata[offset + 3] & 0xFFL);
    }

    private static void writeU32(byte[] out, int offset, long value) {
        out[offset] = (byte) ((value >> 24) & 0xFF);
        out[offset + 1] = (byte) ((value >> 16) & 0xFF);
        out[offset + 2] = (byte) ((value >> 8) & 0xFF);
        out[offset + 3] = (byte) (value & 0xFF);
    }

    private static void validateU32(long value, String field) {
        if (value < 0 || value > 0xFFFF_FFFFL) {
            throw new IllegalArgumentException("CSYNC " + field + " must be between 0 and 4294967295");
        }
    }
}
