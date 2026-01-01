// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Domain model for DNS TSIG record RDATA.
 *
 * <p>RDATA is algorithm name, time signed, fudge, MAC, original ID,
 * error, and other data.</p>
 
 *
 * <pre>
 * +-------------+--------------+----------------------------+
 * | Field       | Size (octets)| Description                |
 * +-------------+--------------+----------------------------+
 * | Algorithm   | variable     | Algorithm name.            |
 * | TimeSigned  | 6            | Time signed.               |
 * | Fudge       | 2            | Fudge time.                |
 * | MACSize     | 2            | MAC length.                |
 * | MAC         | variable     | MAC bytes.                 |
 * | OrigID      | 2            | Original message ID.       |
 * | Error       | 2            | Error code.                |
 * | OtherLength | 2            | Other data length.         |
 * | OtherData   | variable     | Other data bytes.          |
 * +-------------+--------------+----------------------------+
 * </pre>

 * <p>Purpose: Carries Transaction SIGnature (TSIG) data used to authenticate DNS messages.</p>
 * <p>RFC: <a href="https://www.rfc-editor.org/rfc/rfc8945">RFC 8945</a>.</p>*/
@Getter
@Builder
@ToString
@MetaRDataType
public class TsigRecordDataDom implements RDataDom {

    public static RDataDom from(byte[] rdata) {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }

    @Override
    public byte[] to() {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }
}
