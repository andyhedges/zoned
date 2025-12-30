// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CdsRecordDataDomTest {

    @Test
    void fromRejectsInvalidLength() {
        assertThrows(IllegalArgumentException.class, () -> CdsRecordDataDom.from(null));
        assertThrows(IllegalArgumentException.class, () -> CdsRecordDataDom.from(new byte[4]));
    }

    @Test
    void fromRejectsEmptyDigest() {
        byte[] rdata = new byte[] {0x30, 0x39, 0x08, 0x01};
        assertThrows(IllegalArgumentException.class, () -> CdsRecordDataDom.from(rdata));
    }

    @Test
    void fromParsesFields() {
        byte[] digest = new byte[] {1, 2, 3, 4};
        byte[] rdata = new byte[] {0x30, 0x39, 0x08, 0x01, 1, 2, 3, 4};

        RDataDom dom = CdsRecordDataDom.from(rdata);
        CdsRecordDataDom cds = assertInstanceOf(CdsRecordDataDom.class, dom);

        assertEquals(12345, cds.keyTag());
        assertEquals(8, cds.algorithm());
        assertEquals(1, cds.digestType());
        assertArrayEquals(digest, cds.digest());
    }

    @Test
    void toRejectsInvalidFields() {
        CdsRecordDataDom missingDigest = CdsRecordDataDom.builder()
                .keyTag(12345)
                .algorithm(8)
                .digestType(1)
                .build();
        CdsRecordDataDom emptyDigest = CdsRecordDataDom.builder()
                .keyTag(12345)
                .algorithm(8)
                .digestType(1)
                .digest(new byte[0])
                .build();
        CdsRecordDataDom negativeKeyTag = CdsRecordDataDom.builder()
                .keyTag(-1)
                .algorithm(8)
                .digestType(1)
                .digest(new byte[] {1})
                .build();
        CdsRecordDataDom tooLargeKeyTag = CdsRecordDataDom.builder()
                .keyTag(0x1_0000)
                .algorithm(8)
                .digestType(1)
                .digest(new byte[] {1})
                .build();
        CdsRecordDataDom badAlgorithm = CdsRecordDataDom.builder()
                .keyTag(12345)
                .algorithm(256)
                .digestType(1)
                .digest(new byte[] {1})
                .build();
        CdsRecordDataDom negativeAlgorithm = CdsRecordDataDom.builder()
                .keyTag(12345)
                .algorithm(-1)
                .digestType(1)
                .digest(new byte[] {1})
                .build();
        CdsRecordDataDom badDigestType = CdsRecordDataDom.builder()
                .keyTag(12345)
                .algorithm(8)
                .digestType(256)
                .digest(new byte[] {1})
                .build();
        CdsRecordDataDom negativeDigestType = CdsRecordDataDom.builder()
                .keyTag(12345)
                .algorithm(8)
                .digestType(-1)
                .digest(new byte[] {1})
                .build();

        assertThrows(IllegalArgumentException.class, missingDigest::to);
        assertThrows(IllegalArgumentException.class, emptyDigest::to);
        assertThrows(IllegalArgumentException.class, negativeKeyTag::to);
        assertThrows(IllegalArgumentException.class, tooLargeKeyTag::to);
        assertThrows(IllegalArgumentException.class, badAlgorithm::to);
        assertThrows(IllegalArgumentException.class, negativeAlgorithm::to);
        assertThrows(IllegalArgumentException.class, badDigestType::to);
        assertThrows(IllegalArgumentException.class, negativeDigestType::to);
    }

    @Test
    void toSerializesRdata() {
        byte[] digest = new byte[] {1, 2, 3, 4};
        CdsRecordDataDom dom = CdsRecordDataDom.builder()
                .keyTag(12345)
                .algorithm(8)
                .digestType(1)
                .digest(digest)
                .build();

        byte[] expected = new byte[] {0x30, 0x39, 0x08, 0x01, 1, 2, 3, 4};
        assertArrayEquals(expected, dom.to());
    }

    @Test
    void roundTripPreservesFields() {
        byte[] digest = new byte[] {9, 8, 7};
        CdsRecordDataDom original = CdsRecordDataDom.builder()
                .keyTag(123)
                .algorithm(5)
                .digestType(2)
                .digest(digest)
                .build();

        RDataDom decoded = CdsRecordDataDom.from(original.to());
        CdsRecordDataDom parsed = assertInstanceOf(CdsRecordDataDom.class, decoded);

        assertEquals(123, parsed.keyTag());
        assertEquals(5, parsed.algorithm());
        assertEquals(2, parsed.digestType());
        assertArrayEquals(digest, parsed.digest());
    }
}
