// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Domain model for DNS KEY record RDATA.
 *
 * <p>RDATA is flags (16-bit), protocol (8-bit), algorithm (8-bit), and public key.</p>
 
 *
 * <pre>
 * +-----------+--------------+----------------------------+
 * | Field     | Size (octets)| Description                |
 * +-----------+--------------+----------------------------+
 * | Flags     | 2            | Key flags.                 |
 * | Protocol  | 1            | Protocol number.           |
 * | Algorithm | 1            | DNSSEC algorithm.          |
 * | PublicKey | variable     | Public key bytes.          |
 * +-----------+--------------+----------------------------+
 * </pre>
*/
@Getter
@Builder
@ToString
public class KeyRecordDataDom implements RDataDom {

    public static RDataDom from(byte[] rdata) {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }

    @Override
    public byte[] to() {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }
}
