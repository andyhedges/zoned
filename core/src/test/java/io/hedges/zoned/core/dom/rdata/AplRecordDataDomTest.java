// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AplRecordDataDomTest {

    @Test
    void fromRejectsInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> AplRecordDataDom.from(null));
        assertThrows(IllegalArgumentException.class, () -> AplRecordDataDom.from(new byte[0]));
        assertThrows(IllegalArgumentException.class, () -> AplRecordDataDom.from(new byte[3]));
        assertThrows(IllegalArgumentException.class, () -> AplRecordDataDom.from(new byte[] {0, 1, 24, 5}));
    }

    @Test
    void fromParsesEntries() {
        byte[] rdata = TestBytes.concat(
                new byte[] {0, 1, 24, 3, (byte) 192, 0, 2},
                new byte[] {0, 2, 32, 4, 0x20, 0x01, 0x0d, (byte) 0xb8}
        );

        RDataDom dom = AplRecordDataDom.from(rdata);
        AplRecordDataDom apl = assertInstanceOf(AplRecordDataDom.class, dom);

        assertEquals(2, apl.entries().size());
        AplRecordDataDom.AplEntry first = apl.entries().get(0);
        assertEquals(1, first.addressFamily());
        assertEquals(24, first.prefixLength());
        assertFalse(first.negation());
        assertArrayEquals(new byte[] {(byte) 192, 0, 2}, first.address());

        AplRecordDataDom.AplEntry second = apl.entries().get(1);
        assertEquals(2, second.addressFamily());
        assertEquals(32, second.prefixLength());
        assertFalse(second.negation());
        assertArrayEquals(new byte[] {0x20, 0x01, 0x0d, (byte) 0xb8}, second.address());
    }

    @Test
    void fromParsesNegationFlag() {
        byte[] rdata = new byte[] {0, 1, 8, (byte) 0x81, 10};

        RDataDom dom = AplRecordDataDom.from(rdata);
        AplRecordDataDom apl = assertInstanceOf(AplRecordDataDom.class, dom);

        assertEquals(1, apl.entries().size());
        AplRecordDataDom.AplEntry entry = apl.entries().get(0);
        assertTrue(entry.negation());
        assertEquals(1, entry.addressFamily());
        assertEquals(8, entry.prefixLength());
        assertArrayEquals(new byte[] {10}, entry.address());
    }

    @Test
    void toRejectsInvalidEntries() {
        assertThrows(IllegalArgumentException.class, () -> AplRecordDataDom.builder().build().to());
        assertThrows(IllegalArgumentException.class, () -> AplRecordDataDom.builder().entries(List.of()).build().to());
        assertThrows(IllegalArgumentException.class, () -> AplRecordDataDom.builder().entries(java.util.Arrays.asList((AplRecordDataDom.AplEntry) null)).build().to());

        AplRecordDataDom.AplEntry nullAddress = AplRecordDataDom.AplEntry.builder()
                .addressFamily(1)
                .prefixLength(8)
                .address(null)
                .build();
        assertThrows(IllegalArgumentException.class, () -> AplRecordDataDom.builder().entries(List.of(nullAddress)).build().to());

        AplRecordDataDom.AplEntry badPrefix = AplRecordDataDom.AplEntry.builder()
                .addressFamily(1)
                .prefixLength(256)
                .address(new byte[] {10})
                .build();
        assertThrows(IllegalArgumentException.class, () -> AplRecordDataDom.builder().entries(List.of(badPrefix)).build().to());

        AplRecordDataDom.AplEntry tooShort = AplRecordDataDom.AplEntry.builder()
                .addressFamily(1)
                .prefixLength(16)
                .address(new byte[] {10})
                .build();
        assertThrows(IllegalArgumentException.class, () -> AplRecordDataDom.builder().entries(List.of(tooShort)).build().to());
    }

    @Test
    void toSerializesEntries() {
        AplRecordDataDom.AplEntry ipv4 = AplRecordDataDom.AplEntry.builder()
                .addressFamily(1)
                .prefixLength(24)
                .negation(false)
                .address(new byte[] {(byte) 192, 0, 2})
                .build();
        AplRecordDataDom.AplEntry ipv6 = AplRecordDataDom.AplEntry.builder()
                .addressFamily(2)
                .prefixLength(32)
                .negation(true)
                .address(new byte[] {0x20, 0x01, 0x0d, (byte) 0xb8})
                .build();
        AplRecordDataDom dom = AplRecordDataDom.builder().entries(List.of(ipv4, ipv6)).build();

        byte[] encoded = dom.to();

        assertArrayEquals(
                TestBytes.concat(
                        new byte[] {0, 1, 24, 3, (byte) 192, 0, 2},
                        new byte[] {0, 2, 32, (byte) 0x84, 0x20, 0x01, 0x0d, (byte) 0xb8}
                ),
                encoded
        );
    }

    @Test
    void roundTripPreservesEntries() {
        AplRecordDataDom.AplEntry ipv4 = AplRecordDataDom.AplEntry.builder()
                .addressFamily(1)
                .prefixLength(24)
                .negation(false)
                .address(new byte[] {(byte) 203, 0, 113})
                .build();
        AplRecordDataDom.AplEntry ipv6 = AplRecordDataDom.AplEntry.builder()
                .addressFamily(2)
                .prefixLength(32)
                .negation(true)
                .address(new byte[] {0x20, 0x01, 0x0d, (byte) 0xb8})
                .build();
        AplRecordDataDom original = AplRecordDataDom.builder().entries(List.of(ipv4, ipv6)).build();

        RDataDom decoded = AplRecordDataDom.from(original.to());
        AplRecordDataDom parsed = assertInstanceOf(AplRecordDataDom.class, decoded);

        assertEquals(2, parsed.entries().size());
        assertEquals(1, parsed.entries().get(0).addressFamily());
        assertEquals(24, parsed.entries().get(0).prefixLength());
        assertFalse(parsed.entries().get(0).negation());
        assertArrayEquals(new byte[] {(byte) 203, 0, 113}, parsed.entries().get(0).address());
        assertEquals(2, parsed.entries().get(1).addressFamily());
        assertEquals(32, parsed.entries().get(1).prefixLength());
        assertTrue(parsed.entries().get(1).negation());
        assertArrayEquals(new byte[] {0x20, 0x01, 0x0d, (byte) 0xb8}, parsed.entries().get(1).address());
    }
}
