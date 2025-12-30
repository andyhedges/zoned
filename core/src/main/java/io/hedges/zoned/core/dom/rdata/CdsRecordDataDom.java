// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Domain model for DNS CDS record RDATA.
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

 * <p>Purpose: Publishes child DS records to signal the parent which DS values to publish for DNSSEC delegation.</p>
 * <p>RFC: <a href="https://www.rfc-editor.org/rfc/rfc7344">RFC 7344</a>.</p>*/
@Getter
@Builder
@ToString
public class CdsRecordDataDom implements RDataDom {

    public static RDataDom from(byte[] rdata) {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }

    @Override
    public byte[] to() {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }
}
