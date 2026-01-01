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
 * <p>By publishing CDS in the child zone, operators can automate DS updates in the parent and
 * coordinate DNSSEC key rollovers with minimal manual intervention.</p>
 * <p>RFC: <a href="https://www.rfc-editor.org/rfc/rfc7344">RFC 7344</a>.</p>*/
@Getter
@Builder
@ToString
public class CdsRecordDataDom implements RDataDom {
    private int keyTag;
    private int algorithm;
    private int digestType;
    private byte[] digest;

    public static RDataDom from(byte[] rdata) {
        if (rdata == null || rdata.length < 5) {
            throw new IllegalArgumentException("CDS RDATA requires key tag, algorithm, digest type, and digest");
        }
        int keyTag = RDataUtils.readU16(rdata, 0);
        int algorithm = RDataUtils.readU8(rdata, 2);
        int digestType = RDataUtils.readU8(rdata, 3);
        int digestLength = rdata.length - 4;
        if (digestLength <= 0) {
            throw new IllegalArgumentException("CDS digest must not be empty");
        }
        byte[] digest = new byte[digestLength];
        System.arraycopy(rdata, 4, digest, 0, digestLength);
        return CdsRecordDataDom.builder()
                .keyTag(keyTag)
                .algorithm(algorithm)
                .digestType(digestType)
                .digest(digest)
                .build();
    }

    @Override
    public byte[] to() {
        if (keyTag < 0 || keyTag > 0xFFFF) {
            throw new IllegalArgumentException("CDS key tag must be between 0 and 65535");
        }
        if (algorithm < 0 || algorithm > 0xFF) {
            throw new IllegalArgumentException("CDS algorithm must be between 0 and 255");
        }
        if (digestType < 0 || digestType > 0xFF) {
            throw new IllegalArgumentException("CDS digest type must be between 0 and 255");
        }
        if (digest == null || digest.length == 0) {
            throw new IllegalArgumentException("CDS digest must not be empty");
        }
        byte[] out = new byte[4 + digest.length];
        out[0] = (byte) ((keyTag >> 8) & 0xFF);
        out[1] = (byte) (keyTag & 0xFF);
        out[2] = (byte) (algorithm & 0xFF);
        out[3] = (byte) (digestType & 0xFF);
        System.arraycopy(digest, 0, out, 4, digest.length);
        return out;
    }
}
