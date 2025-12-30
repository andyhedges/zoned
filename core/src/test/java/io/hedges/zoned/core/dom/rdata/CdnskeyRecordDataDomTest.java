// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CdnskeyRecordDataDomTest {

    @Test
    void fromRejectsTooShort() {
        assertThrows(IllegalArgumentException.class, () -> CdnskeyRecordDataDom.from(null));
        assertThrows(IllegalArgumentException.class, () -> CdnskeyRecordDataDom.from(new byte[3]));
        assertThrows(IllegalArgumentException.class, () -> CdnskeyRecordDataDom.from(new byte[4]));
    }

    @Test
    void fromParsesFields() {
        byte[] key = new byte[] {1, 2, 3, 4};
        byte[] rdata = new byte[] {0x01, 0x01, 0x03, 0x08, 1, 2, 3, 4};

        RDataDom dom = CdnskeyRecordDataDom.from(rdata);
        CdnskeyRecordDataDom cdnskey = assertInstanceOf(CdnskeyRecordDataDom.class, dom);

        assertEquals(257, cdnskey.flags());
        assertEquals(3, cdnskey.protocol());
        assertEquals(8, cdnskey.algorithm());
        assertArrayEquals(key, cdnskey.publicKey());
    }

    @Test
    void toRejectsInvalidFields() {
        CdnskeyRecordDataDom missingKey = CdnskeyRecordDataDom.builder()
                .flags(257)
                .protocol(3)
                .algorithm(8)
                .build();
        CdnskeyRecordDataDom emptyKey = CdnskeyRecordDataDom.builder()
                .flags(257)
                .protocol(3)
                .algorithm(8)
                .publicKey(new byte[0])
                .build();
        CdnskeyRecordDataDom negativeFlags = CdnskeyRecordDataDom.builder()
                .flags(-1)
                .protocol(3)
                .algorithm(8)
                .publicKey(new byte[] {1})
                .build();
        CdnskeyRecordDataDom tooLargeFlags = CdnskeyRecordDataDom.builder()
                .flags(0x1_0000)
                .protocol(3)
                .algorithm(8)
                .publicKey(new byte[] {1})
                .build();
        CdnskeyRecordDataDom badProtocol = CdnskeyRecordDataDom.builder()
                .flags(257)
                .protocol(256)
                .algorithm(8)
                .publicKey(new byte[] {1})
                .build();
        CdnskeyRecordDataDom badAlgorithm = CdnskeyRecordDataDom.builder()
                .flags(257)
                .protocol(3)
                .algorithm(256)
                .publicKey(new byte[] {1})
                .build();

        assertThrows(IllegalArgumentException.class, missingKey::to);
        assertThrows(IllegalArgumentException.class, emptyKey::to);
        assertThrows(IllegalArgumentException.class, negativeFlags::to);
        assertThrows(IllegalArgumentException.class, tooLargeFlags::to);
        assertThrows(IllegalArgumentException.class, badProtocol::to);
        assertThrows(IllegalArgumentException.class, badAlgorithm::to);
    }

    @Test
    void toSerializesRdata() {
        byte[] key = new byte[] {1, 2, 3, 4};
        CdnskeyRecordDataDom dom = CdnskeyRecordDataDom.builder()
                .flags(257)
                .protocol(3)
                .algorithm(8)
                .publicKey(key)
                .build();

        byte[] expected = new byte[] {0x01, 0x01, 0x03, 0x08, 1, 2, 3, 4};
        assertArrayEquals(expected, dom.to());
    }

    @Test
    void roundTripPreservesFields() {
        byte[] key = new byte[] {9, 8, 7};
        CdnskeyRecordDataDom original = CdnskeyRecordDataDom.builder()
                .flags(256)
                .protocol(3)
                .algorithm(13)
                .publicKey(key)
                .build();

        RDataDom decoded = CdnskeyRecordDataDom.from(original.to());
        CdnskeyRecordDataDom parsed = assertInstanceOf(CdnskeyRecordDataDom.class, decoded);

        assertEquals(256, parsed.flags());
        assertEquals(3, parsed.protocol());
        assertEquals(13, parsed.algorithm());
        assertArrayEquals(key, parsed.publicKey());
    }
}
