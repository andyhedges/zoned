// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Domain model for the ANY meta-type.
 *
 * <p>ANY has no defined RDATA; it is used for queries rather than data storage.</p>
 
 *
 * <pre>
 * +--------+--------------+---------------------------+
 * | Field  | Size (octets)| Description               |
 * +--------+--------------+---------------------------+
 * | RDATA  | 0            | Not used for ANY records. |
 * +--------+--------------+---------------------------+
 * </pre>
*/
@Getter
@Builder
@ToString
public class AnyRecordDataDom implements RDataDom {

    public static RDataDom from(byte[] rdata) {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }

    @Override
    public byte[] to() {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }
}
