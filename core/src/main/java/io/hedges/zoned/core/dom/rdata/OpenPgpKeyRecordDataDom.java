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
 * <p>Keys are typically published under a hashed name derived from the local part of an email address,
 * allowing clients to discover and validate the correct public key via DNS.</p>
 * <p>RFC: <a href="https://www.rfc-editor.org/rfc/rfc7929">RFC 7929</a>.</p>*/
@Getter
@Builder
@ToString
public class OpenPgpKeyRecordDataDom implements RDataDom {
    private byte[] publicKey;

    public static RDataDom from(byte[] rdata) {
        if (rdata == null || rdata.length == 0) {
            throw new IllegalArgumentException("OPENPGPKEY RDATA must not be empty");
        }
        byte[] publicKey = new byte[rdata.length];
        System.arraycopy(rdata, 0, publicKey, 0, rdata.length);
        return OpenPgpKeyRecordDataDom.builder()
                .publicKey(publicKey)
                .build();
    }

    @Override
    public byte[] to() {
        if (publicKey == null || publicKey.length == 0) {
            throw new IllegalArgumentException("OPENPGPKEY public key must not be empty");
        }
        byte[] out = new byte[publicKey.length];
        System.arraycopy(publicKey, 0, out, 0, publicKey.length);
        return out;
    }
}
