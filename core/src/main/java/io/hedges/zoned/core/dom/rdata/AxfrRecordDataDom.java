// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Domain model for AXFR meta-type RDATA.
 *
 * <p>AXFR has no defined RDATA; it is used for zone transfers.</p>
 
 *
 * <pre>
 * +--------+--------------+------------------------------+
 * | Field  | Size (octets)| Description                  |
 * +--------+--------------+------------------------------+
 * | RDATA  | 0            | Not used for AXFR records.   |
 * +--------+--------------+------------------------------+
 * </pre>
*/
@Getter
@Builder
@ToString
public class AxfrRecordDataDom implements RDataDom {

    public static RDataDom from(byte[] rdata) {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }

    @Override
    public byte[] to() {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }
}
