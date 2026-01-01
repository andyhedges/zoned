// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Domain model for DNS CDNSKEY record RDATA.
 *
 * <p>RDATA is flags (16-bit), protocol (8-bit), algorithm (8-bit), and public key.</p>
 
 *
 * <pre>
 * +-----------+--------------+----------------------------+
 * | Field     | Size (octets)| Description                |
 * +-----------+--------------+----------------------------+
 * | Flags     | 2            | Key flags.                 |
 * | Protocol  | 1            | Protocol number.           |
 * | Algorithm | 1            | DNSSEC algorithm.          |
 * | PublicKey | variable     | Public key bytes.          |
 * +-----------+--------------+----------------------------+
 * </pre>

 * <p>Purpose: Publishes child DNSKEY records to signal the parent which keys to use for Delegation Signer (DS) synchronization.</p>
 * <p>It allows a child zone to convey DNSKEY material directly, enabling automated DS management by the parent
 * without out-of-band key exchange.</p>
 * <p>RFC: <a href="https://www.rfc-editor.org/rfc/rfc7344">RFC 7344</a>.</p>*/
@Getter
@Builder
@ToString
public class CdnskeyRecordDataDom implements RDataDom {
    private int flags;
    private int protocol;
    private int algorithm;
    private byte[] publicKey;

    public static RDataDom from(byte[] rdata) {
        if (rdata == null || rdata.length < 4) {
            throw new IllegalArgumentException("CDNSKEY RDATA requires flags, protocol, and algorithm");
        }
        int flags = RDataUtils.readU16(rdata, 0);
        int protocol = RDataUtils.readU8(rdata, 2);
        int algorithm = RDataUtils.readU8(rdata, 3);
        int keyLength = rdata.length - 4;
        if (keyLength <= 0) {
            throw new IllegalArgumentException("CDNSKEY public key must not be empty");
        }
        byte[] publicKey = new byte[keyLength];
        System.arraycopy(rdata, 4, publicKey, 0, keyLength);
        return CdnskeyRecordDataDom.builder()
                .flags(flags)
                .protocol(protocol)
                .algorithm(algorithm)
                .publicKey(publicKey)
                .build();
    }

    @Override
    public byte[] to() {
        if (flags < 0 || flags > 0xFFFF) {
            throw new IllegalArgumentException("CDNSKEY flags must be between 0 and 65535");
        }
        if (protocol < 0 || protocol > 0xFF) {
            throw new IllegalArgumentException("CDNSKEY protocol must be between 0 and 255");
        }
        if (algorithm < 0 || algorithm > 0xFF) {
            throw new IllegalArgumentException("CDNSKEY algorithm must be between 0 and 255");
        }
        if (publicKey == null || publicKey.length == 0) {
            throw new IllegalArgumentException("CDNSKEY public key must not be empty");
        }
        byte[] out = new byte[4 + publicKey.length];
        out[0] = (byte) ((flags >> 8) & 0xFF);
        out[1] = (byte) (flags & 0xFF);
        out[2] = (byte) (protocol & 0xFF);
        out[3] = (byte) (algorithm & 0xFF);
        System.arraycopy(publicKey, 0, out, 4, publicKey.length);
        return out;
    }
}
