// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import io.hedges.zoned.core.dom.rdata.edns.EdnsOptionDom;
import io.hedges.zoned.core.dom.rdata.edns.UnknownEdnsOptionDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
@Getter
@Builder
@ToString
public class OptRecordDataDom implements RDataDom {
    private int udpPayloadSize;
    private int extendedRCode;
    private int version;
    private boolean dnssecOk;
    private List<EdnsOptionDom> ednsOptions;

    public static RDataDom from(byte[] rdata) {
        return OptRecordDataDom.builder()
                .udpPayloadSize(0)
                .extendedRCode(0)
                .version(0)
                .dnssecOk(false)
                .ednsOptions(parseOptions(rdata))
                .build();
    }

    public static OptRecordDataDom from(byte[] rdata, int udpPayloadSize, long ttl) {
        int extendedRCode = (int) ((ttl >> 24) & 0xFF);
        int version = (int) ((ttl >> 16) & 0xFF);
        boolean dnssecOk = (ttl & 0x8000) != 0;
        return OptRecordDataDom.builder()
                .udpPayloadSize(udpPayloadSize)
                .extendedRCode(extendedRCode)
                .version(version)
                .dnssecOk(dnssecOk)
                .ednsOptions(parseOptions(rdata))
                .build();
    }

    @Override
    public byte[] to() {
        if (ednsOptions == null || ednsOptions.isEmpty()) {
            return new byte[0];
        }
        int totalLength = 0;
        List<byte[]> encoded = new ArrayList<>(ednsOptions.size());
        for (EdnsOptionDom option : ednsOptions) {
            if (option instanceof UnknownEdnsOptionDom unknown) {
                byte[] data = unknown.getData();
                int dataLength = data == null ? 0 : data.length;
                byte[] bytes = new byte[4 + dataLength];
                bytes[0] = (byte) ((unknown.getCode() >> 8) & 0xFF);
                bytes[1] = (byte) (unknown.getCode() & 0xFF);
                bytes[2] = (byte) ((dataLength >> 8) & 0xFF);
                bytes[3] = (byte) (dataLength & 0xFF);
                if (dataLength > 0) {
                    System.arraycopy(data, 0, bytes, 4, dataLength);
                }
                encoded.add(bytes);
                totalLength += bytes.length;
            }
        }
        byte[] rdata = new byte[totalLength];
        int offset = 0;
        for (byte[] bytes : encoded) {
            System.arraycopy(bytes, 0, rdata, offset, bytes.length);
            offset += bytes.length;
        }
        return rdata;
    }

    private static List<EdnsOptionDom> parseOptions(byte[] rdata) {
        if (rdata == null || rdata.length == 0) {
            return List.of();
        }
        List<EdnsOptionDom> options = new ArrayList<>();
        int idx = 0;
        while (idx + 4 <= rdata.length) {
            int code = RDataUtils.readU16(rdata, idx);
            int length = RDataUtils.readU16(rdata, idx + 2);
            idx += 4;
            if (length < 0 || idx + length > rdata.length) {
                break;
            }
            byte[] data = new byte[length];
            if (length > 0) {
                System.arraycopy(rdata, idx, data, 0, length);
            }
            options.add(new UnknownEdnsOptionDom(code, data));
            idx += length;
        }
        return options;
    }
}
