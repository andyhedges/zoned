// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Domain model for DNS RRSIG record RDATA.
 *
 * <p>RDATA is type covered (16-bit), algorithm (8-bit), labels (8-bit),
 * original TTL (32-bit), signature expiration (32-bit), signature inception (32-bit),
 * key tag (16-bit), signer name, and signature.</p>
 
 *
 * <pre>
 * +-------------+--------------+----------------------------+
 * | Field       | Size (octets)| Description                |
 * +-------------+--------------+----------------------------+
 * | TypeCovered | 2            | Covered RR type.           |
 * | Algorithm   | 1            | DNSSEC algorithm.          |
 * | Labels      | 1            | Label count.               |
 * | OriginalTTL | 4            | Original TTL.              |
 * | SigExpire   | 4            | Signature expiration.      |
 * | SigIncept   | 4            | Signature inception.       |
 * | KeyTag      | 2            | Key tag.                   |
 * | SignerName  | variable     | Signer name.               |
 * | Signature   | variable     | Signature bytes.           |
 * +-------------+--------------+----------------------------+
 * </pre>

 * <p>Purpose: Holds DNSSEC signatures (RRSIG) that cover an RRset for validation.</p>
 * <p>RFC: <a href="https://www.rfc-editor.org/rfc/rfc4034">RFC 4034</a>.</p>*/
@Getter
@Builder
@ToString
public class RrsigRecordDataDom implements RDataDom {

    public static RDataDom from(byte[] rdata) {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }

    @Override
    public byte[] to() {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }
}
