// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Domain model for DNS IPSECKEY record RDATA.
 *
 * <p>RDATA is precedence (8-bit), gateway type (8-bit), algorithm (8-bit),
 * gateway (variable), and public key (variable).</p>
 
 *
 * <pre>
 * +-----------+--------------+----------------------------+
 * | Field     | Size (octets)| Description                |
 * +-----------+--------------+----------------------------+
 * | Precedence| 1            | Gateway precedence.        |
 * | GatewayTy | 1            | Gateway type.              |
 * | Algorithm | 1            | Public key algorithm.      |
 * | Gateway   | variable     | Gateway bytes or name.     |
 * | PublicKey | variable     | Public key bytes.          |
 * +-----------+--------------+----------------------------+
 * </pre>
*/
@Getter
@Builder
@ToString
public class IpseckeyRecordDataDom implements RDataDom {

    public static RDataDom from(byte[] rdata) {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }

    @Override
    public byte[] to() {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }
}
