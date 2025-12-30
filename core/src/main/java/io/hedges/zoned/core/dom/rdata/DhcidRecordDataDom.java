// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Domain model for DNS DHCID record RDATA.
 *
 * <p>RDATA is an opaque identifier blob.</p>
 
 *
 * <pre>
 * +-----------+--------------+----------------------------+
 * | Field     | Size (octets)| Description                |
 * +-----------+--------------+----------------------------+
 * | Identifier| variable     | DHCID identifier bytes.    |
 * +-----------+--------------+----------------------------+
 * </pre>
*/
@Getter
@Builder
@ToString
public class DhcidRecordDataDom implements RDataDom {
    private byte[] identifier;

    public static RDataDom from(byte[] rdata) {
        if (rdata == null || rdata.length == 0) {
            throw new IllegalArgumentException("DHCID RDATA must not be empty");
        }
        byte[] identifier = new byte[rdata.length];
        System.arraycopy(rdata, 0, identifier, 0, rdata.length);
        return DhcidRecordDataDom.builder()
                .identifier(identifier)
                .build();
    }

    @Override
    public byte[] to() {
        if (identifier == null || identifier.length == 0) {
            throw new IllegalArgumentException("DHCID identifier must not be empty");
        }
        byte[] out = new byte[identifier.length];
        System.arraycopy(identifier, 0, out, 0, identifier.length);
        return out;
    }
}
