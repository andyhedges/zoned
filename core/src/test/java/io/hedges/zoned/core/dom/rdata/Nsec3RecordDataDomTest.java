// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Nsec3RecordDataDomTest {

    @Test
    void fromRejectsInvalidLength() {
        assertThrows(IllegalArgumentException.class, () -> Nsec3RecordDataDom.from(null));
        assertThrows(IllegalArgumentException.class, () -> Nsec3RecordDataDom.from(new byte[5]));
    }

    @Test
    void fromRejectsSaltLengthMismatch() {
        byte[] rdata = new byte[] {1, 0, 0, 1, 2, 1};
        assertThrows(IllegalArgumentException.class, () -> Nsec3RecordDataDom.from(rdata));
    }

    @Test
    void fromRejectsNextOwnerLengthMismatch() {
        byte[] rdata = new byte[] {1, 0, 0, 1, 0, 2, 1};
        assertThrows(IllegalArgumentException.class, () -> Nsec3RecordDataDom.from(rdata));
    }

    @Test
    void fromRejectsMissingTypeBitmaps() {
        byte[] rdata = new byte[] {1, 0, 0, 1, 0, 0};
        assertThrows(IllegalArgumentException.class, () -> Nsec3RecordDataDom.from(rdata));
    }

    @Test
    void fromParsesFields() {
        byte[] rdata = new byte[] {1, 2, 0, 10, 2, 9, 8, 3, 7, 6, 5, 1, 2};
        RDataDom dom = Nsec3RecordDataDom.from(rdata);
        Nsec3RecordDataDom nsec3 = assertInstanceOf(Nsec3RecordDataDom.class, dom);

        assertEquals(1, nsec3.hashAlgorithm());
        assertEquals(2, nsec3.flags());
        assertEquals(10, nsec3.iterations());
        assertArrayEquals(new byte[] {9, 8}, nsec3.salt());
        assertArrayEquals(new byte[] {7, 6, 5}, nsec3.nextHashedOwner());
        assertArrayEquals(new byte[] {1, 2}, nsec3.typeBitmaps());
    }

    @Test
    void toRejectsInvalidFields() {
        Nsec3RecordDataDom negativeHash = Nsec3RecordDataDom.builder()
                .hashAlgorithm(-1)
                .flags(0)
                .iterations(0)
                .salt(new byte[0])
                .nextHashedOwner(new byte[0])
                .typeBitmaps(new byte[] {1})
                .build();
        Nsec3RecordDataDom tooLargeHash = Nsec3RecordDataDom.builder()
                .hashAlgorithm(256)
                .flags(0)
                .iterations(0)
                .salt(new byte[0])
                .nextHashedOwner(new byte[0])
                .typeBitmaps(new byte[] {1})
                .build();
        Nsec3RecordDataDom negativeFlags = Nsec3RecordDataDom.builder()
                .hashAlgorithm(1)
                .flags(-1)
                .iterations(0)
                .salt(new byte[0])
                .nextHashedOwner(new byte[0])
                .typeBitmaps(new byte[] {1})
                .build();
        Nsec3RecordDataDom tooLargeFlags = Nsec3RecordDataDom.builder()
                .hashAlgorithm(1)
                .flags(256)
                .iterations(0)
                .salt(new byte[0])
                .nextHashedOwner(new byte[0])
                .typeBitmaps(new byte[] {1})
                .build();
        Nsec3RecordDataDom negativeIterations = Nsec3RecordDataDom.builder()
                .hashAlgorithm(1)
                .flags(0)
                .iterations(-1)
                .salt(new byte[0])
                .nextHashedOwner(new byte[0])
                .typeBitmaps(new byte[] {1})
                .build();
        Nsec3RecordDataDom tooLargeIterations = Nsec3RecordDataDom.builder()
                .hashAlgorithm(1)
                .flags(0)
                .iterations(0x1_0000)
                .salt(new byte[0])
                .nextHashedOwner(new byte[0])
                .typeBitmaps(new byte[] {1})
                .build();
        Nsec3RecordDataDom nullSalt = Nsec3RecordDataDom.builder()
                .hashAlgorithm(1)
                .flags(0)
                .iterations(0)
                .nextHashedOwner(new byte[0])
                .typeBitmaps(new byte[] {1})
                .build();
        Nsec3RecordDataDom tooLongSalt = Nsec3RecordDataDom.builder()
                .hashAlgorithm(1)
                .flags(0)
                .iterations(0)
                .salt(new byte[256])
                .nextHashedOwner(new byte[0])
                .typeBitmaps(new byte[] {1})
                .build();
        Nsec3RecordDataDom nullNext = Nsec3RecordDataDom.builder()
                .hashAlgorithm(1)
                .flags(0)
                .iterations(0)
                .salt(new byte[0])
                .typeBitmaps(new byte[] {1})
                .build();
        Nsec3RecordDataDom tooLongNext = Nsec3RecordDataDom.builder()
                .hashAlgorithm(1)
                .flags(0)
                .iterations(0)
                .salt(new byte[0])
                .nextHashedOwner(new byte[256])
                .typeBitmaps(new byte[] {1})
                .build();
        Nsec3RecordDataDom nullTypes = Nsec3RecordDataDom.builder()
                .hashAlgorithm(1)
                .flags(0)
                .iterations(0)
                .salt(new byte[0])
                .nextHashedOwner(new byte[0])
                .build();
        Nsec3RecordDataDom emptyTypes = Nsec3RecordDataDom.builder()
                .hashAlgorithm(1)
                .flags(0)
                .iterations(0)
                .salt(new byte[0])
                .nextHashedOwner(new byte[0])
                .typeBitmaps(new byte[0])
                .build();

        assertThrows(IllegalArgumentException.class, negativeHash::to);
        assertThrows(IllegalArgumentException.class, tooLargeHash::to);
        assertThrows(IllegalArgumentException.class, negativeFlags::to);
        assertThrows(IllegalArgumentException.class, tooLargeFlags::to);
        assertThrows(IllegalArgumentException.class, negativeIterations::to);
        assertThrows(IllegalArgumentException.class, tooLargeIterations::to);
        assertThrows(IllegalArgumentException.class, nullSalt::to);
        assertThrows(IllegalArgumentException.class, tooLongSalt::to);
        assertThrows(IllegalArgumentException.class, nullNext::to);
        assertThrows(IllegalArgumentException.class, tooLongNext::to);
        assertThrows(IllegalArgumentException.class, nullTypes::to);
        assertThrows(IllegalArgumentException.class, emptyTypes::to);
    }

    @Test
    void toSerializesRdata() {
        Nsec3RecordDataDom dom = Nsec3RecordDataDom.builder()
                .hashAlgorithm(1)
                .flags(2)
                .iterations(10)
                .salt(new byte[] {9, 8})
                .nextHashedOwner(new byte[] {7, 6, 5})
                .typeBitmaps(new byte[] {1, 2})
                .build();

        byte[] expected = new byte[] {1, 2, 0, 10, 2, 9, 8, 3, 7, 6, 5, 1, 2};
        assertArrayEquals(expected, dom.to());
    }

    @Test
    void roundTripPreservesFields() {
        Nsec3RecordDataDom original = Nsec3RecordDataDom.builder()
                .hashAlgorithm(1)
                .flags(0)
                .iterations(5)
                .salt(new byte[] {1})
                .nextHashedOwner(new byte[] {2, 3})
                .typeBitmaps(new byte[] {4, 5})
                .build();

        RDataDom decoded = Nsec3RecordDataDom.from(original.to());
        Nsec3RecordDataDom parsed = assertInstanceOf(Nsec3RecordDataDom.class, decoded);

        assertEquals(1, parsed.hashAlgorithm());
        assertEquals(0, parsed.flags());
        assertEquals(5, parsed.iterations());
        assertArrayEquals(new byte[] {1}, parsed.salt());
        assertArrayEquals(new byte[] {2, 3}, parsed.nextHashedOwner());
        assertArrayEquals(new byte[] {4, 5}, parsed.typeBitmaps());
    }
}
