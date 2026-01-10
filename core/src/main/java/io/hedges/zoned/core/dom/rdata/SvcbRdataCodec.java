// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.NameResolver;
import io.hedges.zoned.core.dom.DnsNameDom;
import io.hedges.zoned.core.dom.DnsNameDomPolicy;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

final class SvcbRdataCodec {

    private SvcbRdataCodec() {
    }

    static SvcbRdataFields parse(byte[] rdata, NameResolver resolver, String typeLabel) {
        String label = typeLabel == null ? "SVCB" : typeLabel;
        if (rdata == null || rdata.length < 3) {
            throw new IllegalArgumentException(label + " RDATA requires priority and target name");
        }
        int svcPriority = RDataUtils.readU16(rdata, 0);
        RDataUtils.DnsNameParseResult parsedName = RDataUtils.parseDnsName(
                rdata,
                2,
                resolver,
                DnsNameDomPolicy.Builtin.HOSTNAME);
        DnsNameDom targetName = parsedName.name();
        int idx = parsedName.nextIndex();

        SortedMap<Integer, byte[]> params = new TreeMap<>();
        while (idx < rdata.length) {
            if (idx + 4 > rdata.length) {
                throw new IllegalArgumentException(label + " service parameter header is truncated");
            }
            int key = RDataUtils.readU16(rdata, idx);
            int len = RDataUtils.readU16(rdata, idx + 2);
            idx += 4;
            if (idx + len > rdata.length) {
                throw new IllegalArgumentException(label + " service parameter exceeds RDATA bounds");
            }
            byte[] value = new byte[len];
            if (len > 0) {
                System.arraycopy(rdata, idx, value, 0, len);
            }
            if (params.putIfAbsent(key, value) != null) {
                throw new IllegalArgumentException("Duplicate " + label + " service parameter key: " + key);
            }
            idx += len;
        }

        return new SvcbRdataFields(svcPriority, targetName, params);
    }

    static byte[] encode(int svcPriority, DnsNameDom targetName, SortedMap<Integer, byte[]> svcParams, String typeLabel) {
        String label = typeLabel == null ? "SVCB" : typeLabel;
        if (targetName == null) {
            throw new IllegalArgumentException(label + " RDATA requires a target name");
        }
        if (svcPriority < 0 || svcPriority > 0xFFFF) {
            throw new IllegalArgumentException(label + " SvcPriority must be between 0 and 65535");
        }
        byte[] nameBytes = RDataUtils.toByteArray(targetName);
        int totalLength = 2 + nameBytes.length;
        if (svcParams != null) {
            for (Map.Entry<Integer, byte[]> entry : svcParams.entrySet()) {
                if (entry.getKey() == null) {
                    throw new IllegalArgumentException(label + " service parameter key is null");
                }
                int key = entry.getKey();
                if (key < 0 || key > 0xFFFF) {
                    throw new IllegalArgumentException(label + " service parameter key out of range: " + key);
                }
                byte[] value = entry.getValue();
                if (value == null) {
                    throw new IllegalArgumentException(label + " service parameter value is null for key: " + key);
                }
                if (value.length > 0xFFFF) {
                    throw new IllegalArgumentException(label + " service parameter value exceeds 65535 bytes");
                }
                if (totalLength + 4 + value.length > 0xFFFF) {
                    throw new IllegalArgumentException(label + " RDATA exceeds 65535 bytes");
                }
                totalLength += 4 + value.length;
            }
        }

        byte[] out = new byte[totalLength];
        int idx = 0;
        out[idx++] = (byte) ((svcPriority >> 8) & 0xFF);
        out[idx++] = (byte) (svcPriority & 0xFF);
        System.arraycopy(nameBytes, 0, out, idx, nameBytes.length);
        idx += nameBytes.length;
        if (svcParams != null) {
            for (Map.Entry<Integer, byte[]> entry : svcParams.entrySet()) {
                int key = entry.getKey();
                byte[] value = entry.getValue();
                int len = value.length;
                out[idx++] = (byte) ((key >> 8) & 0xFF);
                out[idx++] = (byte) (key & 0xFF);
                out[idx++] = (byte) ((len >> 8) & 0xFF);
                out[idx++] = (byte) (len & 0xFF);
                if (len > 0) {
                    System.arraycopy(value, 0, out, idx, len);
                }
                idx += len;
            }
        }
        if (idx != out.length) {
            throw new IllegalStateException("Bug: " + label + " RDATA encoded length mismatch");
        }
        return out;
    }

    record SvcbRdataFields(int svcPriority, DnsNameDom targetName, SortedMap<Integer, byte[]> svcParams) {
    }
}
