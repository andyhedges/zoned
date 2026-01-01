// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Domain model for DNS TA record RDATA.
 *
 * <p>RDATA is key tag (16-bit), algorithm (8-bit), digest type (8-bit), and digest.</p>
 
 *
 * <pre>
 * +-----------+--------------+----------------------------+
 * | Field     | Size (octets)| Description                |
 * +-----------+--------------+----------------------------+
 * | KeyTag    | 2            | Key tag.                   |
 * | Algorithm | 1            | DNSSEC algorithm.          |
 * | DigestType| 1            | Digest type.               |
 * | Digest    | variable     | Digest bytes.              |
 * +-----------+--------------+----------------------------+
 * </pre>

 * <p>Purpose: Publishes DNSSEC trust anchors in the legacy TA record format (obsolete).</p>
 * <p>TA records were used for distributing trust anchors but are superseded by modern DNSSEC practices
 * such as managed DNSKEY/DS trust anchors.</p>
 * <p>RFC: <a href="https://www.rfc-editor.org/rfc/rfc4431">RFC 4431</a>.</p>*/
@Getter
@Builder
@ToString
public class TaRecordDataDom implements RDataDom {

    public static RDataDom from(byte[] rdata) {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }

    @Override
    public byte[] to() {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }
}
