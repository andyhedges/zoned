// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DlvRecordDataDomTest {

    @Test
    void fromRejectsInvalidLength() {
        assertThrows(IllegalArgumentException.class, () -> DlvRecordDataDom.from(null));
        assertThrows(IllegalArgumentException.class, () -> DlvRecordDataDom.from(new byte[4]));
    }

    @Test
    void fromParsesFields() {
        byte[] digest = new byte[] {1, 2, 3, 4};
        byte[] rdata = new byte[] {0x30, 0x39, 0x08, 0x01, 1, 2, 3, 4};

        RDataDom dom = DlvRecordDataDom.from(rdata);
        DlvRecordDataDom dlv = assertInstanceOf(DlvRecordDataDom.class, dom);

        assertEquals(12345, dlv.keyTag());
        assertEquals(8, dlv.algorithm());
        assertEquals(1, dlv.digestType());
        assertArrayEquals(digest, dlv.digest());
    }

    @Test
    void toRejectsInvalidFields() {
        DlvRecordDataDom missingDigest = DlvRecordDataDom.builder()
                .keyTag(12345)
                .algorithm(8)
                .digestType(1)
                .build();
        DlvRecordDataDom emptyDigest = DlvRecordDataDom.builder()
                .keyTag(12345)
                .algorithm(8)
                .digestType(1)
                .digest(new byte[0])
                .build();
        DlvRecordDataDom negativeKeyTag = DlvRecordDataDom.builder()
                .keyTag(-1)
                .algorithm(8)
                .digestType(1)
                .digest(new byte[] {1})
                .build();
        DlvRecordDataDom tooLargeKeyTag = DlvRecordDataDom.builder()
                .keyTag(0x1_0000)
                .algorithm(8)
                .digestType(1)
                .digest(new byte[] {1})
                .build();
        DlvRecordDataDom badAlgorithm = DlvRecordDataDom.builder()
                .keyTag(12345)
                .algorithm(256)
                .digestType(1)
                .digest(new byte[] {1})
                .build();
        DlvRecordDataDom badDigestType = DlvRecordDataDom.builder()
                .keyTag(12345)
                .algorithm(8)
                .digestType(256)
                .digest(new byte[] {1})
                .build();

        assertThrows(IllegalArgumentException.class, missingDigest::to);
        assertThrows(IllegalArgumentException.class, emptyDigest::to);
        assertThrows(IllegalArgumentException.class, negativeKeyTag::to);
        assertThrows(IllegalArgumentException.class, tooLargeKeyTag::to);
        assertThrows(IllegalArgumentException.class, badAlgorithm::to);
        assertThrows(IllegalArgumentException.class, badDigestType::to);
    }

    @Test
    void toSerializesRdata() {
        byte[] digest = new byte[] {1, 2, 3, 4};
        DlvRecordDataDom dom = DlvRecordDataDom.builder()
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
        DlvRecordDataDom original = DlvRecordDataDom.builder()
                .keyTag(123)
                .algorithm(5)
                .digestType(2)
                .digest(digest)
                .build();

        RDataDom decoded = DlvRecordDataDom.from(original.to());
        DlvRecordDataDom parsed = assertInstanceOf(DlvRecordDataDom.class, decoded);

        assertEquals(123, parsed.keyTag());
        assertEquals(5, parsed.algorithm());
        assertEquals(2, parsed.digestType());
        assertArrayEquals(digest, parsed.digest());
    }
}
