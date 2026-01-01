// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.NameResolver;
import io.hedges.zoned.core.dom.DnsNameDom;
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
 * <p>Validators check the signature with the referenced DNSKEY and the inception/expiration times to
 * ensure the data is authentic and current.</p>
 * <p>RFC: <a href="https://www.rfc-editor.org/rfc/rfc4034">RFC 4034</a>.</p>*/
@Getter
@Builder
@ToString
@CompressableRData
public class RrsigRecordDataDom implements RDataDom {
    private int typeCovered;
    private int algorithm;
    private int labels;
    private long originalTtl;
    private long signatureExpiration;
    private long signatureInception;
    private int keyTag;
    private DnsNameDom signerName;
    private byte[] signature;

    public static RDataDom from(byte[] rdata, NameResolver resolver) {
        RrsigRdataCodec.SigFields fields = RrsigRdataCodec.parse(rdata, resolver, "RRSIG");
        return RrsigRecordDataDom.builder()
                .typeCovered(fields.typeCovered())
                .algorithm(fields.algorithm())
                .labels(fields.labels())
                .originalTtl(fields.originalTtl())
                .signatureExpiration(fields.signatureExpiration())
                .signatureInception(fields.signatureInception())
                .keyTag(fields.keyTag())
                .signerName(fields.signerName())
                .signature(fields.signature())
                .build();
    }

    @Override
    public byte[] to() {
        return RrsigRdataCodec.encode(
                typeCovered,
                algorithm,
                labels,
                originalTtl,
                signatureExpiration,
                signatureInception,
                keyTag,
                signerName,
                signature,
                "RRSIG"
        );
    }
}
