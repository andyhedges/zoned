// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DnskeyRecordDataDomTest {

    @Test
    void fromRejectsTooShort() {
        assertThrows(IllegalArgumentException.class, () -> DnskeyRecordDataDom.from(null));
        assertThrows(IllegalArgumentException.class, () -> DnskeyRecordDataDom.from(new byte[3]));
        assertThrows(IllegalArgumentException.class, () -> DnskeyRecordDataDom.from(new byte[4]));
    }

    @Test
    void fromParsesFields() {
        byte[] key = new byte[] {1, 2, 3, 4};
        byte[] rdata = new byte[] {0x01, 0x01, 0x03, 0x08, 1, 2, 3, 4};

        RDataDom dom = DnskeyRecordDataDom.from(rdata);
        DnskeyRecordDataDom dnskey = assertInstanceOf(DnskeyRecordDataDom.class, dom);

        assertEquals(257, dnskey.flags());
        assertEquals(3, dnskey.protocol());
        assertEquals(8, dnskey.algorithm());
        assertArrayEquals(key, dnskey.publicKey());
    }

    @Test
    void toRejectsInvalidFields() {
        DnskeyRecordDataDom missingKey = DnskeyRecordDataDom.builder()
                .flags(257)
                .protocol(3)
                .algorithm(8)
                .build();
        DnskeyRecordDataDom emptyKey = DnskeyRecordDataDom.builder()
                .flags(257)
                .protocol(3)
                .algorithm(8)
                .publicKey(new byte[0])
                .build();
        DnskeyRecordDataDom negativeFlags = DnskeyRecordDataDom.builder()
                .flags(-1)
                .protocol(3)
                .algorithm(8)
                .publicKey(new byte[] {1})
                .build();
        DnskeyRecordDataDom tooLargeFlags = DnskeyRecordDataDom.builder()
                .flags(0x1_0000)
                .protocol(3)
                .algorithm(8)
                .publicKey(new byte[] {1})
                .build();
        DnskeyRecordDataDom badProtocol = DnskeyRecordDataDom.builder()
                .flags(257)
                .protocol(256)
                .algorithm(8)
                .publicKey(new byte[] {1})
                .build();
        DnskeyRecordDataDom negativeProtocol = DnskeyRecordDataDom.builder()
                .flags(257)
                .protocol(-1)
                .algorithm(8)
                .publicKey(new byte[] {1})
                .build();
        DnskeyRecordDataDom badAlgorithm = DnskeyRecordDataDom.builder()
                .flags(257)
                .protocol(3)
                .algorithm(256)
                .publicKey(new byte[] {1})
                .build();
        DnskeyRecordDataDom negativeAlgorithm = DnskeyRecordDataDom.builder()
                .flags(257)
                .protocol(3)
                .algorithm(-1)
                .publicKey(new byte[] {1})
                .build();

        assertThrows(IllegalArgumentException.class, missingKey::to);
        assertThrows(IllegalArgumentException.class, emptyKey::to);
        assertThrows(IllegalArgumentException.class, negativeFlags::to);
        assertThrows(IllegalArgumentException.class, tooLargeFlags::to);
        assertThrows(IllegalArgumentException.class, badProtocol::to);
        assertThrows(IllegalArgumentException.class, negativeProtocol::to);
        assertThrows(IllegalArgumentException.class, badAlgorithm::to);
        assertThrows(IllegalArgumentException.class, negativeAlgorithm::to);
    }

    @Test
    void toSerializesRdata() {
        byte[] key = new byte[] {1, 2, 3, 4};
        DnskeyRecordDataDom dom = DnskeyRecordDataDom.builder()
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
        DnskeyRecordDataDom original = DnskeyRecordDataDom.builder()
                .flags(256)
                .protocol(3)
                .algorithm(13)
                .publicKey(key)
                .build();

        RDataDom decoded = DnskeyRecordDataDom.from(original.to());
        DnskeyRecordDataDom parsed = assertInstanceOf(DnskeyRecordDataDom.class, decoded);

        assertEquals(256, parsed.flags());
        assertEquals(3, parsed.protocol());
        assertEquals(13, parsed.algorithm());
        assertArrayEquals(key, parsed.publicKey());
    }
}
