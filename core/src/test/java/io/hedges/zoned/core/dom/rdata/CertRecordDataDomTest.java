// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CertRecordDataDomTest {

    @Test
    void fromRejectsTooShort() {
        assertThrows(IllegalArgumentException.class, () -> CertRecordDataDom.from(null));
        assertThrows(IllegalArgumentException.class, () -> CertRecordDataDom.from(new byte[4]));
        assertThrows(IllegalArgumentException.class, () -> CertRecordDataDom.from(new byte[5]));
    }

    @Test
    void fromParsesFields() {
        byte[] cert = new byte[] {0, 1, 2, 3, 4};
        byte[] rdata = new byte[] {0x00, 0x01, 0x30, 0x39, 0x08, 0, 1, 2, 3, 4};

        RDataDom dom = CertRecordDataDom.from(rdata);
        CertRecordDataDom certDom = assertInstanceOf(CertRecordDataDom.class, dom);

        assertEquals(1, certDom.certificateType());
        assertEquals(12345, certDom.keyTag());
        assertEquals(8, certDom.algorithm());
        assertArrayEquals(cert, certDom.certificate());
    }

    @Test
    void toRejectsInvalidFields() {
        CertRecordDataDom missingCert = CertRecordDataDom.builder()
                .certificateType(1)
                .keyTag(12345)
                .algorithm(8)
                .build();
        CertRecordDataDom emptyCert = CertRecordDataDom.builder()
                .certificateType(1)
                .keyTag(12345)
                .algorithm(8)
                .certificate(new byte[0])
                .build();
        CertRecordDataDom negativeType = CertRecordDataDom.builder()
                .certificateType(-1)
                .keyTag(12345)
                .algorithm(8)
                .certificate(new byte[] {1})
                .build();
        CertRecordDataDom tooLargeType = CertRecordDataDom.builder()
                .certificateType(0x1_0000)
                .keyTag(12345)
                .algorithm(8)
                .certificate(new byte[] {1})
                .build();
        CertRecordDataDom negativeKeyTag = CertRecordDataDom.builder()
                .certificateType(1)
                .keyTag(-1)
                .algorithm(8)
                .certificate(new byte[] {1})
                .build();
        CertRecordDataDom tooLargeKeyTag = CertRecordDataDom.builder()
                .certificateType(1)
                .keyTag(0x1_0000)
                .algorithm(8)
                .certificate(new byte[] {1})
                .build();
        CertRecordDataDom badAlgorithm = CertRecordDataDom.builder()
                .certificateType(1)
                .keyTag(12345)
                .algorithm(256)
                .certificate(new byte[] {1})
                .build();
        CertRecordDataDom negativeAlgorithm = CertRecordDataDom.builder()
                .certificateType(1)
                .keyTag(12345)
                .algorithm(-1)
                .certificate(new byte[] {1})
                .build();

        assertThrows(IllegalArgumentException.class, missingCert::to);
        assertThrows(IllegalArgumentException.class, emptyCert::to);
        assertThrows(IllegalArgumentException.class, negativeType::to);
        assertThrows(IllegalArgumentException.class, tooLargeType::to);
        assertThrows(IllegalArgumentException.class, negativeKeyTag::to);
        assertThrows(IllegalArgumentException.class, tooLargeKeyTag::to);
        assertThrows(IllegalArgumentException.class, badAlgorithm::to);
        assertThrows(IllegalArgumentException.class, negativeAlgorithm::to);
    }

    @Test
    void toSerializesRdata() {
        byte[] cert = new byte[] {0, 1, 2, 3, 4};
        CertRecordDataDom dom = CertRecordDataDom.builder()
                .certificateType(1)
                .keyTag(12345)
                .algorithm(8)
                .certificate(cert)
                .build();

        byte[] expected = new byte[] {0x00, 0x01, 0x30, 0x39, 0x08, 0, 1, 2, 3, 4};
        assertArrayEquals(expected, dom.to());
    }

    @Test
    void roundTripPreservesFields() {
        byte[] cert = new byte[] {9, 8, 7};
        CertRecordDataDom original = CertRecordDataDom.builder()
                .certificateType(3)
                .keyTag(54321)
                .algorithm(5)
                .certificate(cert)
                .build();

        RDataDom decoded = CertRecordDataDom.from(original.to());
        CertRecordDataDom parsed = assertInstanceOf(CertRecordDataDom.class, decoded);

        assertEquals(3, parsed.certificateType());
        assertEquals(54321, parsed.keyTag());
        assertEquals(5, parsed.algorithm());
        assertArrayEquals(cert, parsed.certificate());
    }
}
