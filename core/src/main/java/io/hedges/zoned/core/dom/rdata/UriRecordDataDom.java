// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Domain model for DNS URI record RDATA.
 *
 * <p>RDATA is priority (16-bit), weight (16-bit), and target URI bytes.</p>
 
 *
 * <pre>
 * +-----------+--------------+----------------------------+
 * | Field     | Size (octets)| Description                |
 * +-----------+--------------+----------------------------+
 * | Priority  | 2            | URI priority.              |
 * | Weight    | 2            | URI weight.                |
 * | Target    | variable     | URI bytes.                 |
 * +-----------+--------------+----------------------------+
 * </pre>

 * <p>Purpose: Publishes Uniform Resource Identifier (URI) data for a domain name.</p>
 * <p>Priority and weight enable selection among multiple URIs, similar to SRV, and allow clients
 * to discover application-specific endpoints via DNS.</p>
 * <p>RFC: <a href="https://www.rfc-editor.org/rfc/rfc7553">RFC 7553</a>.</p>*/
@Getter
@Builder
@ToString
public class UriRecordDataDom implements RDataDom {

    public static RDataDom from(byte[] rdata) {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }

    @Override
    public byte[] to() {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }
}
