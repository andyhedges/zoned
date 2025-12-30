// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Domain model for DNS OPENPGPKEY record RDATA.
 *
 * <p>RDATA is the OpenPGP public key data.</p>
 
 *
 * <pre>
 * +-----------+--------------+----------------------------+
 * | Field     | Size (octets)| Description                |
 * +-----------+--------------+----------------------------+
 * | PublicKey | variable     | OpenPGP public key bytes.  |
 * +-----------+--------------+----------------------------+
 * </pre>

 * <p>Purpose: Publishes OpenPGP public keys in DNS for email or identity verification.</p>
 * <p>RFC: <a href="https://www.rfc-editor.org/rfc/rfc7929">RFC 7929</a>.</p>*/
@Getter
@Builder
@ToString
public class OpenpgpkeyRecordDataDom implements RDataDom {

    public static RDataDom from(byte[] rdata) {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }

    @Override
    public byte[] to() {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }
}
