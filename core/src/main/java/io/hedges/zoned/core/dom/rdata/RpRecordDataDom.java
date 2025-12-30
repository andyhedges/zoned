// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Domain model for DNS RP record RDATA.
 *
 * <p>RDATA is mailbox domain name and text domain name.</p>
 
 *
 * <pre>
 * +-----------+--------------+----------------------------+
 * | Field     | Size (octets)| Description                |
 * +-----------+--------------+----------------------------+
 * | MboxDNAME | variable     | Mailbox domain name.       |
 * | TxtDNAME  | variable     | Text domain name.          |
 * +-----------+--------------+----------------------------+
 * </pre>

 * <p>Purpose: Provides Responsible Person (RP) contact information and a related text domain.</p>
 * <p>RFC: <a href="https://www.rfc-editor.org/rfc/rfc1183">RFC 1183</a>.</p>*/
@Getter
@Builder
@ToString
public class RpRecordDataDom implements RDataDom {

    public static RDataDom from(byte[] rdata) {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }

    @Override
    public byte[] to() {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }
}
