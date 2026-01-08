// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.NameResolver;
import io.hedges.zoned.core.dom.DnsNameDom;
import io.hedges.zoned.core.dom.RDataDom;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RrsigRecordDataDomTest {
    private static final NameResolver RESOLVER =
            offset -> DnsNameDom.labels(List.of("sig", "example"));

    @Test
    void fromRejectsTooShort() {
        assertThrows(IllegalArgumentException.class, () -> RrsigRecordDataDom.from(null, RESOLVER));
        assertThrows(IllegalArgumentException.class, () -> RrsigRecordDataDom.from(new byte[18], RESOLVER));
    }

    @Test
    void fromRejectsMissingSignature() {
        byte[] rdata = new byte[] {
                0, 1, 1, 2,
                0, 0, 0, 1,
                0, 0, 0, 2,
                0, 0, 0, 3,
                0, 4,
                1, 'a', 0
        };
        assertThrows(IllegalArgumentException.class, () -> RrsigRecordDataDom.from(rdata, RESOLVER));
    }

    @Test
    void fromParsesFields() {
        byte[] signer = new byte[] {3, 's', 'i', 'g', 7, 'e', 'x', 'a', 'm', 'p', 'l', 'e', 4, 't', 'e', 's', 't', 0};
        byte[] signature = new byte[] {9, 8, 7};
        byte[] rdata = new byte[18 + signer.length + signature.length];
        int idx = 0;
        rdata[idx++] = 0;
        rdata[idx++] = 1;
        rdata[idx++] = 2;
        rdata[idx++] = 3;
        rdata[idx++] = 0;
        rdata[idx++] = 0;
        rdata[idx++] = 0;
        rdata[idx++] = 10;
        rdata[idx++] = 0;
        rdata[idx++] = 0;
        rdata[idx++] = 0;
        rdata[idx++] = 11;
        rdata[idx++] = 0;
        rdata[idx++] = 0;
        rdata[idx++] = 0;
        rdata[idx++] = 12;
        rdata[idx++] = 0;
        rdata[idx++] = 13;
        System.arraycopy(signer, 0, rdata, idx, signer.length);
        idx += signer.length;
        System.arraycopy(signature, 0, rdata, idx, signature.length);

        RDataDom dom = RrsigRecordDataDom.from(rdata, RESOLVER);
        RrsigRecordDataDom rrsig = assertInstanceOf(RrsigRecordDataDom.class, dom);

        assertEquals(1, rrsig.typeCovered());
        assertEquals(2, rrsig.algorithm());
        assertEquals(3, rrsig.labels());
        assertEquals(10, rrsig.originalTtl());
        assertEquals(11, rrsig.signatureExpiration());
        assertEquals(12, rrsig.signatureInception());
        assertEquals(13, rrsig.keyTag());
        assertEquals(List.of("sig", "example", "test"), rrsig.signerName().labelStrings());
        assertArrayEquals(signature, rrsig.signature());
    }

    @Test
    void fromUsesResolverForCompressedNames() {
        byte[] signature = new byte[] {1, 2, 3};
        byte[] rdata = new byte[] {
                0, 1, 1, 2,
                0, 0, 0, 1,
                0, 0, 0, 2,
                0, 0, 0, 3,
                0, 4,
                (byte) 0xC0, 0x10,
                1, 2, 3
        };

        RDataDom dom = RrsigRecordDataDom.from(rdata, RESOLVER);
        RrsigRecordDataDom rrsig = assertInstanceOf(RrsigRecordDataDom.class, dom);

        assertEquals(List.of("sig", "example"), rrsig.signerName().labelStrings());
        assertArrayEquals(signature, rrsig.signature());
    }

    @Test
    void toRejectsInvalidFields() {
        RrsigRecordDataDom negativeType = RrsigRecordDataDom.builder()
                .typeCovered(-1)
                .algorithm(1)
                .labels(1)
                .originalTtl(0)
                .signatureExpiration(0)
                .signatureInception(0)
                .keyTag(1)
                .signerName(DnsNameDom.labels(List.of("sig", "example")))
                .signature(new byte[] {1})
                .build();
        RrsigRecordDataDom tooLargeType = RrsigRecordDataDom.builder()
                .typeCovered(0x1_0000)
                .algorithm(1)
                .labels(1)
                .originalTtl(0)
                .signatureExpiration(0)
                .signatureInception(0)
                .keyTag(1)
                .signerName(DnsNameDom.labels(List.of("sig", "example")))
                .signature(new byte[] {1})
                .build();
        RrsigRecordDataDom negativeAlg = RrsigRecordDataDom.builder()
                .typeCovered(1)
                .algorithm(-1)
                .labels(1)
                .originalTtl(0)
                .signatureExpiration(0)
                .signatureInception(0)
                .keyTag(1)
                .signerName(DnsNameDom.labels(List.of("sig", "example")))
                .signature(new byte[] {1})
                .build();
        RrsigRecordDataDom tooLargeAlg = RrsigRecordDataDom.builder()
                .typeCovered(1)
                .algorithm(256)
                .labels(1)
                .originalTtl(0)
                .signatureExpiration(0)
                .signatureInception(0)
                .keyTag(1)
                .signerName(DnsNameDom.labels(List.of("sig", "example")))
                .signature(new byte[] {1})
                .build();
        RrsigRecordDataDom negativeLabels = RrsigRecordDataDom.builder()
                .typeCovered(1)
                .algorithm(1)
                .labels(-1)
                .originalTtl(0)
                .signatureExpiration(0)
                .signatureInception(0)
                .keyTag(1)
                .signerName(DnsNameDom.labels(List.of("sig", "example")))
                .signature(new byte[] {1})
                .build();
        RrsigRecordDataDom tooLargeLabels = RrsigRecordDataDom.builder()
                .typeCovered(1)
                .algorithm(1)
                .labels(256)
                .originalTtl(0)
                .signatureExpiration(0)
                .signatureInception(0)
                .keyTag(1)
                .signerName(DnsNameDom.labels(List.of("sig", "example")))
                .signature(new byte[] {1})
                .build();
        RrsigRecordDataDom negativeTtl = RrsigRecordDataDom.builder()
                .typeCovered(1)
                .algorithm(1)
                .labels(1)
                .originalTtl(-1)
                .signatureExpiration(0)
                .signatureInception(0)
                .keyTag(1)
                .signerName(DnsNameDom.labels(List.of("sig", "example")))
                .signature(new byte[] {1})
                .build();
        RrsigRecordDataDom tooLargeTtl = RrsigRecordDataDom.builder()
                .typeCovered(1)
                .algorithm(1)
                .labels(1)
                .originalTtl(0x1_0000_0000L)
                .signatureExpiration(0)
                .signatureInception(0)
                .keyTag(1)
                .signerName(DnsNameDom.labels(List.of("sig", "example")))
                .signature(new byte[] {1})
                .build();
        RrsigRecordDataDom negativeExpire = RrsigRecordDataDom.builder()
                .typeCovered(1)
                .algorithm(1)
                .labels(1)
                .originalTtl(0)
                .signatureExpiration(-1)
                .signatureInception(0)
                .keyTag(1)
                .signerName(DnsNameDom.labels(List.of("sig", "example")))
                .signature(new byte[] {1})
                .build();
        RrsigRecordDataDom tooLargeExpire = RrsigRecordDataDom.builder()
                .typeCovered(1)
                .algorithm(1)
                .labels(1)
                .originalTtl(0)
                .signatureExpiration(0x1_0000_0000L)
                .signatureInception(0)
                .keyTag(1)
                .signerName(DnsNameDom.labels(List.of("sig", "example")))
                .signature(new byte[] {1})
                .build();
        RrsigRecordDataDom negativeIncept = RrsigRecordDataDom.builder()
                .typeCovered(1)
                .algorithm(1)
                .labels(1)
                .originalTtl(0)
                .signatureExpiration(0)
                .signatureInception(-1)
                .keyTag(1)
                .signerName(DnsNameDom.labels(List.of("sig", "example")))
                .signature(new byte[] {1})
                .build();
        RrsigRecordDataDom tooLargeIncept = RrsigRecordDataDom.builder()
                .typeCovered(1)
                .algorithm(1)
                .labels(1)
                .originalTtl(0)
                .signatureExpiration(0)
                .signatureInception(0x1_0000_0000L)
                .keyTag(1)
                .signerName(DnsNameDom.labels(List.of("sig", "example")))
                .signature(new byte[] {1})
                .build();
        RrsigRecordDataDom negativeKeyTag = RrsigRecordDataDom.builder()
                .typeCovered(1)
                .algorithm(1)
                .labels(1)
                .originalTtl(0)
                .signatureExpiration(0)
                .signatureInception(0)
                .keyTag(-1)
                .signerName(DnsNameDom.labels(List.of("sig", "example")))
                .signature(new byte[] {1})
                .build();
        RrsigRecordDataDom tooLargeKeyTag = RrsigRecordDataDom.builder()
                .typeCovered(1)
                .algorithm(1)
                .labels(1)
                .originalTtl(0)
                .signatureExpiration(0)
                .signatureInception(0)
                .keyTag(0x1_0000)
                .signerName(DnsNameDom.labels(List.of("sig", "example")))
                .signature(new byte[] {1})
                .build();
        RrsigRecordDataDom missingSigner = RrsigRecordDataDom.builder()
                .typeCovered(1)
                .algorithm(1)
                .labels(1)
                .originalTtl(0)
                .signatureExpiration(0)
                .signatureInception(0)
                .keyTag(1)
                .signature(new byte[] {1})
                .build();
        RrsigRecordDataDom missingSignature = RrsigRecordDataDom.builder()
                .typeCovered(1)
                .algorithm(1)
                .labels(1)
                .originalTtl(0)
                .signatureExpiration(0)
                .signatureInception(0)
                .keyTag(1)
                .signerName(DnsNameDom.labels(List.of("sig", "example")))
                .build();
        RrsigRecordDataDom emptySignature = RrsigRecordDataDom.builder()
                .typeCovered(1)
                .algorithm(1)
                .labels(1)
                .originalTtl(0)
                .signatureExpiration(0)
                .signatureInception(0)
                .keyTag(1)
                .signerName(DnsNameDom.labels(List.of("sig", "example")))
                .signature(new byte[0])
                .build();

        assertThrows(IllegalArgumentException.class, negativeType::to);
        assertThrows(IllegalArgumentException.class, tooLargeType::to);
        assertThrows(IllegalArgumentException.class, negativeAlg::to);
        assertThrows(IllegalArgumentException.class, tooLargeAlg::to);
        assertThrows(IllegalArgumentException.class, negativeLabels::to);
        assertThrows(IllegalArgumentException.class, tooLargeLabels::to);
        assertThrows(IllegalArgumentException.class, negativeTtl::to);
        assertThrows(IllegalArgumentException.class, tooLargeTtl::to);
        assertThrows(IllegalArgumentException.class, negativeExpire::to);
        assertThrows(IllegalArgumentException.class, tooLargeExpire::to);
        assertThrows(IllegalArgumentException.class, negativeIncept::to);
        assertThrows(IllegalArgumentException.class, tooLargeIncept::to);
        assertThrows(IllegalArgumentException.class, negativeKeyTag::to);
        assertThrows(IllegalArgumentException.class, tooLargeKeyTag::to);
        assertThrows(IllegalArgumentException.class, missingSigner::to);
        assertThrows(IllegalArgumentException.class, missingSignature::to);
        assertThrows(IllegalArgumentException.class, emptySignature::to);
    }

    @Test
    void toSerializesRdata() {
        DnsNameDom signer = DnsNameDom.labels(List.of("sig", "example", "test"));
        RrsigRecordDataDom dom = RrsigRecordDataDom.builder()
                .typeCovered(1)
                .algorithm(2)
                .labels(3)
                .originalTtl(10)
                .signatureExpiration(11)
                .signatureInception(12)
                .keyTag(13)
                .signerName(signer)
                .signature(new byte[] {9, 8, 7})
                .build();

        byte[] signerBytes = RDataUtils.toByteArray(signer);
        byte[] expected = new byte[18 + signerBytes.length + 3];
        int idx = 0;
        expected[idx++] = 0;
        expected[idx++] = 1;
        expected[idx++] = 2;
        expected[idx++] = 3;
        expected[idx++] = 0;
        expected[idx++] = 0;
        expected[idx++] = 0;
        expected[idx++] = 10;
        expected[idx++] = 0;
        expected[idx++] = 0;
        expected[idx++] = 0;
        expected[idx++] = 11;
        expected[idx++] = 0;
        expected[idx++] = 0;
        expected[idx++] = 0;
        expected[idx++] = 12;
        expected[idx++] = 0;
        expected[idx++] = 13;
        System.arraycopy(signerBytes, 0, expected, idx, signerBytes.length);
        idx += signerBytes.length;
        expected[idx++] = 9;
        expected[idx++] = 8;
        expected[idx] = 7;

        assertArrayEquals(expected, dom.to());
    }

    @Test
    void roundTripPreservesFields() {
        DnsNameDom signer = DnsNameDom.labels(List.of("sig", "example", "test"));
        RrsigRecordDataDom original = RrsigRecordDataDom.builder()
                .typeCovered(1)
                .algorithm(2)
                .labels(3)
                .originalTtl(10)
                .signatureExpiration(11)
                .signatureInception(12)
                .keyTag(13)
                .signerName(signer)
                .signature(new byte[] {9, 8, 7})
                .build();

        RDataDom decoded = RrsigRecordDataDom.from(original.to(), RESOLVER);
        RrsigRecordDataDom parsed = assertInstanceOf(RrsigRecordDataDom.class, decoded);

        assertEquals(1, parsed.typeCovered());
        assertEquals(2, parsed.algorithm());
        assertEquals(3, parsed.labels());
        assertEquals(10, parsed.originalTtl());
        assertEquals(11, parsed.signatureExpiration());
        assertEquals(12, parsed.signatureInception());
        assertEquals(13, parsed.keyTag());
        assertEquals(List.of("sig", "example", "test"), parsed.signerName().labelStrings());
        assertArrayEquals(new byte[] {9, 8, 7}, parsed.signature());
    }
}
