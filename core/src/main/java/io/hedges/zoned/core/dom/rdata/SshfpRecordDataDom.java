// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Domain model for DNS SSHFP record RDATA.
 *
 * <p>RDATA is algorithm (8-bit), fingerprint type (8-bit), and fingerprint bytes.</p>
 
 *
 * <pre>
 * +-------------+--------------+----------------------------+
 * | Field       | Size (octets)| Description                |
 * +-------------+--------------+----------------------------+
 * | Algorithm   | 1            | SSH key algorithm.         |
 * | FpType      | 1            | Fingerprint type.          |
 * | Fingerprint | variable     | Fingerprint bytes.         |
 * +-------------+--------------+----------------------------+
 * </pre>
*/
@Getter
@Builder
@ToString
public class SshfpRecordDataDom implements RDataDom {

    public static RDataDom from(byte[] rdata) {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }

    @Override
    public byte[] to() {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }
}
