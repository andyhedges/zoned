// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Domain model for DNS TKEY record RDATA.
 *
 * <p>RDATA is algorithm name, inception time, expiration time, mode,
 * error, key data, and other data.</p>
 *
 * <pre>
 * +--------------+--------------+-------------------------------------------+
 * | Field        | Size (octets)| Description                               |
 * +--------------+--------------+-------------------------------------------+
 * | Algorithm    | variable     | Algorithm name (wire format).             |
 * | Inception    | 4            | Start time (seconds since epoch).         |
 * | Expiration   | 4            | End time (seconds since epoch).           |
 * | Mode         | 2            | TKEY mode.                                |
 * | Error        | 2            | Extended error code.                      |
 * | KeyLength    | 2            | Length of key data in octets.             |
 * | KeyData      | variable     | Key material.                             |
 * | OtherLength  | 2            | Length of other data in octets.           |
 * | OtherData    | variable     | Other data bytes.                         |
 * +--------------+--------------+-------------------------------------------+
 * </pre>
 */
@Getter
@Builder
@ToString
public class TkeyRecordDataDom implements RDataDom {

    public static RDataDom from(byte[] rdata) {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }

    @Override
    public byte[] to() {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }
}
