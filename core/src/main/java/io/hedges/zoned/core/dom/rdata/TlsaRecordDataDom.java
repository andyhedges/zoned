// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Domain model for DNS TLSA record RDATA.
 *
 * <p>RDATA is usage (8-bit), selector (8-bit), matching type (8-bit),
 * and certificate association data.</p>
 
 *
 * <pre>
 * +-------------+--------------+----------------------------+
 * | Field       | Size (octets)| Description                |
 * +-------------+--------------+----------------------------+
 * | Usage       | 1            | Usage.                     |
 * | Selector    | 1            | Selector.                  |
 * | MatchType   | 1            | Matching type.             |
 * | AssocData   | variable     | Association data.          |
 * +-------------+--------------+----------------------------+
 * </pre>
*/
@Getter
@Builder
@ToString
public class TlsaRecordDataDom implements RDataDom {

    public static RDataDom from(byte[] rdata) {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }

    @Override
    public byte[] to() {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }
}
