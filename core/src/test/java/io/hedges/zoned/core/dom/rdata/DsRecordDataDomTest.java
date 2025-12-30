// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DsRecordDataDomTest {

    @Test
    void fromRejectsInvalidLength() {
        assertThrows(IllegalArgumentException.class, () -> DsRecordDataDom.from(null));
        assertThrows(IllegalArgumentException.class, () -> DsRecordDataDom.from(new byte[4]));
    }

    @Test
    void fromRejectsEmptyDigest() {
        byte[] rdata = new byte[] {0x30, 0x39, 0x08, 0x01};
        assertThrows(IllegalArgumentException.class, () -> DsRecordDataDom.from(rdata));
    }

    @Test
    void fromParsesFields() {
        byte[] digest = new byte[] {1, 2, 3, 4};
        byte[] rdata = new byte[] {0x30, 0x39, 0x08, 0x01, 1, 2, 3, 4};

        RDataDom dom = DsRecordDataDom.from(rdata);
        DsRecordDataDom ds = assertInstanceOf(DsRecordDataDom.class, dom);

        assertEquals(12345, ds.keyTag());
        assertEquals(8, ds.algorithm());
        assertEquals(1, ds.digestType());
        assertArrayEquals(digest, ds.digest());
    }

    @Test
    void toRejectsInvalidFields() {
        DsRecordDataDom missingDigest = DsRecordDataDom.builder()
                .keyTag(12345)
                .algorithm(8)
                .digestType(1)
                .build();
        DsRecordDataDom emptyDigest = DsRecordDataDom.builder()
                .keyTag(12345)
                .algorithm(8)
                .digestType(1)
                .digest(new byte[0])
                .build();
        DsRecordDataDom negativeKeyTag = DsRecordDataDom.builder()
                .keyTag(-1)
                .algorithm(8)
                .digestType(1)
                .digest(new byte[] {1})
                .build();
        DsRecordDataDom tooLargeKeyTag = DsRecordDataDom.builder()
                .keyTag(0x1_0000)
                .algorithm(8)
                .digestType(1)
                .digest(new byte[] {1})
                .build();
        DsRecordDataDom badAlgorithm = DsRecordDataDom.builder()
                .keyTag(12345)
                .algorithm(256)
                .digestType(1)
                .digest(new byte[] {1})
                .build();
        DsRecordDataDom negativeAlgorithm = DsRecordDataDom.builder()
                .keyTag(12345)
                .algorithm(-1)
                .digestType(1)
                .digest(new byte[] {1})
                .build();
        DsRecordDataDom badDigestType = DsRecordDataDom.builder()
                .keyTag(12345)
                .algorithm(8)
                .digestType(256)
                .digest(new byte[] {1})
                .build();
        DsRecordDataDom negativeDigestType = DsRecordDataDom.builder()
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
        DsRecordDataDom dom = DsRecordDataDom.builder()
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
        DsRecordDataDom original = DsRecordDataDom.builder()
                .keyTag(123)
                .algorithm(5)
                .digestType(2)
                .digest(digest)
                .build();

        RDataDom decoded = DsRecordDataDom.from(original.to());
        DsRecordDataDom parsed = assertInstanceOf(DsRecordDataDom.class, decoded);

        assertEquals(123, parsed.keyTag());
        assertEquals(5, parsed.algorithm());
        assertEquals(2, parsed.digestType());
        assertArrayEquals(digest, parsed.digest());
    }
}
