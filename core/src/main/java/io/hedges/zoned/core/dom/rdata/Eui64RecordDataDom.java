// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Domain model for DNS EUI64 record RDATA.
 *
 * <p>RDATA is an 8-octet EUI-64 identifier.</p>
 
 *
 * <pre>
 * +-----------+--------------+----------------------------+
 * | Field     | Size (octets)| Description                |
 * +-----------+--------------+----------------------------+
 * | Identifier| 8            | EUI-64 bytes.              |
 * +-----------+--------------+----------------------------+
 * </pre>

 * <p>Purpose: Publishes an IEEE Extended Unique Identifier 64-bit (EUI-64) value, often derived from a MAC address.</p>
 * <p>It provides a standardized hardware identifier for device discovery, management, or inventory systems.</p>
 * <p>RFC: <a href="https://www.rfc-editor.org/rfc/rfc7043">RFC 7043</a>.</p>*/
@Getter
@Builder
@ToString
public class Eui64RecordDataDom implements RDataDom {
    private static final int EUI64_LENGTH = 8;
    private byte[] address;

    public static RDataDom from(byte[] rdata) {
        if (rdata == null || rdata.length != EUI64_LENGTH) {
            throw new IllegalArgumentException("EUI64 RDATA must be exactly 8 bytes");
        }
        byte[] address = new byte[EUI64_LENGTH];
        System.arraycopy(rdata, 0, address, 0, EUI64_LENGTH);
        return Eui64RecordDataDom.builder()
                .address(address)
                .build();
    }

    @Override
    public byte[] to() {
        if (address == null || address.length != EUI64_LENGTH) {
            throw new IllegalArgumentException("EUI64 address must be exactly 8 bytes");
        }
        byte[] out = new byte[EUI64_LENGTH];
        System.arraycopy(address, 0, out, 0, EUI64_LENGTH);
        return out;
    }
}
