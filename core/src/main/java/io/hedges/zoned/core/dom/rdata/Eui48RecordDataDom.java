// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class Eui48RecordDataDom implements RDataDom {
    private static final int EUI48_LENGTH = 6;
    private byte[] address;

    public static RDataDom from(byte[] rdata) {
        if (rdata == null || rdata.length != EUI48_LENGTH) {
            throw new IllegalArgumentException("EUI48 RDATA must be exactly 6 bytes");
        }
        byte[] address = new byte[EUI48_LENGTH];
        System.arraycopy(rdata, 0, address, 0, EUI48_LENGTH);
        return Eui48RecordDataDom.builder()
                .address(address)
                .build();
    }

    @Override
    public byte[] to() {
        if (address == null || address.length != EUI48_LENGTH) {
            throw new IllegalArgumentException("EUI48 address must be exactly 6 bytes");
        }
        byte[] out = new byte[EUI48_LENGTH];
        System.arraycopy(address, 0, out, 0, EUI48_LENGTH);
        return out;
    }
}
