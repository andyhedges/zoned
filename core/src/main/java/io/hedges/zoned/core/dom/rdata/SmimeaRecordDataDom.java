// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Domain model for DNS SMIMEA record RDATA.
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

 * <p>Purpose: Publishes S/MIME certificate association data for secure email (S/MIMEA).</p>
 * <p>RFC: <a href="https://www.rfc-editor.org/rfc/rfc8162">RFC 8162</a>.</p>*/
@Getter
@Builder
@ToString
public class SmimeaRecordDataDom implements RDataDom {

    public static RDataDom from(byte[] rdata) {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }

    @Override
    public byte[] to() {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }
}
