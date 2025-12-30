// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Nsec3ParamRecordDataDomTest {

    @Test
    void fromRejectsInvalidLength() {
        assertThrows(IllegalArgumentException.class, () -> Nsec3ParamRecordDataDom.from(null));
        assertThrows(IllegalArgumentException.class, () -> Nsec3ParamRecordDataDom.from(new byte[4]));
    }

    @Test
    void fromRejectsSaltLengthMismatch() {
        byte[] tooShort = new byte[] {1, 0, 0, 1, 2, 1};
        byte[] tooLong = new byte[] {1, 0, 0, 1, 0, 1};
        assertThrows(IllegalArgumentException.class, () -> Nsec3ParamRecordDataDom.from(tooShort));
        assertThrows(IllegalArgumentException.class, () -> Nsec3ParamRecordDataDom.from(tooLong));
    }

    @Test
    void fromParsesFields() {
        byte[] rdata = new byte[] {1, 2, 0, 10, 3, 1, 2, 3};
        RDataDom dom = Nsec3ParamRecordDataDom.from(rdata);
        Nsec3ParamRecordDataDom param = assertInstanceOf(Nsec3ParamRecordDataDom.class, dom);

        assertEquals(1, param.hashAlgorithm());
        assertEquals(2, param.flags());
        assertEquals(10, param.iterations());
        assertArrayEquals(new byte[] {1, 2, 3}, param.salt());
    }

    @Test
    void toRejectsInvalidFields() {
        Nsec3ParamRecordDataDom negativeHash = Nsec3ParamRecordDataDom.builder()
                                                                      .hashAlgorithm(-1)
                                                                      .flags(0)
                                                                      .iterations(0)
                                                                      .salt(new byte[0])
                                                                      .build();
        Nsec3ParamRecordDataDom tooLargeHash = Nsec3ParamRecordDataDom.builder()
                                                                      .hashAlgorithm(256)
                                                                      .flags(0)
                                                                      .iterations(0)
                                                                      .salt(new byte[0])
                                                                      .build();
        Nsec3ParamRecordDataDom negativeFlags = Nsec3ParamRecordDataDom.builder()
                                                                       .hashAlgorithm(1)
                                                                       .flags(-1)
                                                                       .iterations(0)
                                                                       .salt(new byte[0])
                                                                       .build();
        Nsec3ParamRecordDataDom tooLargeFlags = Nsec3ParamRecordDataDom.builder()
                                                                       .hashAlgorithm(1)
                                                                       .flags(256)
                                                                       .iterations(0)
                                                                       .salt(new byte[0])
                                                                       .build();
        Nsec3ParamRecordDataDom negativeIterations = Nsec3ParamRecordDataDom.builder()
                                                                            .hashAlgorithm(1)
                                                                            .flags(0)
                                                                            .iterations(-1)
                                                                            .salt(new byte[0])
                                                                            .build();
        Nsec3ParamRecordDataDom tooLargeIterations = Nsec3ParamRecordDataDom.builder()
                                                                            .hashAlgorithm(1)
                                                                            .flags(0)
                                                                            .iterations(0x1_0000)
                                                                            .salt(new byte[0])
                                                                            .build();
        Nsec3ParamRecordDataDom nullSalt = Nsec3ParamRecordDataDom.builder()
                                                                  .hashAlgorithm(1)
                                                                  .flags(0)
                                                                  .iterations(0)
                                                                  .build();
        Nsec3ParamRecordDataDom tooLongSalt = Nsec3ParamRecordDataDom.builder()
                                                                     .hashAlgorithm(1)
                                                                     .flags(0)
                                                                     .iterations(0)
                                                                     .salt(new byte[256])
                                                                     .build();

        assertThrows(IllegalArgumentException.class, negativeHash::to);
        assertThrows(IllegalArgumentException.class, tooLargeHash::to);
        assertThrows(IllegalArgumentException.class, negativeFlags::to);
        assertThrows(IllegalArgumentException.class, tooLargeFlags::to);
        assertThrows(IllegalArgumentException.class, negativeIterations::to);
        assertThrows(IllegalArgumentException.class, tooLargeIterations::to);
        assertThrows(IllegalArgumentException.class, nullSalt::to);
        assertThrows(IllegalArgumentException.class, tooLongSalt::to);
    }

    @Test
    void toSerializesRdata() {
        Nsec3ParamRecordDataDom dom = Nsec3ParamRecordDataDom.builder()
                                                             .hashAlgorithm(1)
                                                             .flags(2)
                                                             .iterations(10)
                                                             .salt(new byte[] {1, 2, 3})
                                                             .build();

        byte[] expected = new byte[] {1, 2, 0, 10, 3, 1, 2, 3};
        assertArrayEquals(expected, dom.to());
    }

    @Test
    void roundTripPreservesFields() {
        Nsec3ParamRecordDataDom original = Nsec3ParamRecordDataDom.builder()
                                                                  .hashAlgorithm(1)
                                                                  .flags(0)
                                                                  .iterations(5)
                                                                  .salt(new byte[] {9, 8})
                                                                  .build();

        RDataDom decoded = Nsec3ParamRecordDataDom.from(original.to());
        Nsec3ParamRecordDataDom parsed = assertInstanceOf(Nsec3ParamRecordDataDom.class, decoded);

        assertEquals(1, parsed.hashAlgorithm());
        assertEquals(0, parsed.flags());
        assertEquals(5, parsed.iterations());
        assertArrayEquals(new byte[] {9, 8}, parsed.salt());
    }
}
