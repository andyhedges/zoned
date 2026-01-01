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

 * <p>Purpose: Meta-type used for queries to request multiple record types; it has no defined RDATA for storage.</p>
 * <p>ANY is a query-only construct and should not appear as a stored record; responses are implementation
 * dependent and often minimized for performance or privacy.</p>
 * <p>RFC: <a href="https://www.rfc-editor.org/rfc/rfc1035">RFC 1035</a>.</p>*/
@Getter
@Builder
@ToString
@MetaRDataType
public class AnyRecordDataDom implements RDataDom {

    public static RDataDom from(byte[] rdata) {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }

    @Override
    public byte[] to() {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }
}
