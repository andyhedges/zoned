// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SmimeaRecordDataDomTest {

    @Test
    void fromRejectsInvalidLength() {
        assertThrows(IllegalArgumentException.class, () -> SmimeaRecordDataDom.from(null));
        assertThrows(IllegalArgumentException.class, () -> SmimeaRecordDataDom.from(new byte[3]));
    }

    @Test
    void fromRejectsEmptyData() {
        byte[] rdata = new byte[] {1, 2, 3};
        assertThrows(IllegalArgumentException.class, () -> SmimeaRecordDataDom.from(rdata));
    }

    @Test
    void fromParsesFields() {
        byte[] data = sequence(32);
        byte[] rdata = rdataWithData(1, data);
        RDataDom dom = SmimeaRecordDataDom.from(rdata);
        SmimeaRecordDataDom smimea = assertInstanceOf(SmimeaRecordDataDom.class, dom);

        assertEquals(1, smimea.usage());
        assertEquals(2, smimea.selector());
        assertEquals(1, smimea.matchingType());
        assertArrayEquals(data, smimea.associationData());
    }

    @Test
    void fromRejectsInvalidAssociationLength() {
        assertThrows(IllegalArgumentException.class, () -> SmimeaRecordDataDom.from(rdataWithLength(1, 31)));
        assertThrows(IllegalArgumentException.class, () -> SmimeaRecordDataDom.from(rdataWithLength(1, 33)));
        assertThrows(IllegalArgumentException.class, () -> SmimeaRecordDataDom.from(rdataWithLength(2, 63)));
        assertThrows(IllegalArgumentException.class, () -> SmimeaRecordDataDom.from(rdataWithLength(2, 65)));
    }

    @Test
    void toRejectsInvalidFields() {
        SmimeaRecordDataDom missingData = SmimeaRecordDataDom.builder()
                .usage(1)
                .selector(2)
                .matchingType(3)
                .build();
        SmimeaRecordDataDom emptyData = SmimeaRecordDataDom.builder()
                .usage(1)
                .selector(2)
                .matchingType(3)
                .associationData(new byte[0])
                .build();
        SmimeaRecordDataDom negativeUsage = SmimeaRecordDataDom.builder()
                .usage(-1)
                .selector(2)
                .matchingType(3)
                .associationData(new byte[] {1})
                .build();
        SmimeaRecordDataDom tooLargeUsage = SmimeaRecordDataDom.builder()
                .usage(256)
                .selector(2)
                .matchingType(3)
                .associationData(new byte[] {1})
                .build();
        SmimeaRecordDataDom negativeSelector = SmimeaRecordDataDom.builder()
                .usage(1)
                .selector(-1)
                .matchingType(3)
                .associationData(new byte[] {1})
                .build();
        SmimeaRecordDataDom tooLargeSelector = SmimeaRecordDataDom.builder()
                .usage(1)
                .selector(256)
                .matchingType(3)
                .associationData(new byte[] {1})
                .build();
        SmimeaRecordDataDom negativeMatching = SmimeaRecordDataDom.builder()
                .usage(1)
                .selector(2)
                .matchingType(-1)
                .associationData(new byte[] {1})
                .build();
        SmimeaRecordDataDom tooLargeMatching = SmimeaRecordDataDom.builder()
                .usage(1)
                .selector(2)
                .matchingType(256)
                .associationData(new byte[] {1})
                .build();

        assertThrows(IllegalArgumentException.class, missingData::to);
        assertThrows(IllegalArgumentException.class, emptyData::to);
        assertThrows(IllegalArgumentException.class, negativeUsage::to);
        assertThrows(IllegalArgumentException.class, tooLargeUsage::to);
        assertThrows(IllegalArgumentException.class, negativeSelector::to);
        assertThrows(IllegalArgumentException.class, tooLargeSelector::to);
        assertThrows(IllegalArgumentException.class, negativeMatching::to);
        assertThrows(IllegalArgumentException.class, tooLargeMatching::to);
    }

    @Test
    void toRejectsInvalidAssociationLength() {
        SmimeaRecordDataDom sha256Short = SmimeaRecordDataDom.builder()
                .usage(1)
                .selector(2)
                .matchingType(1)
                .associationData(sequence(31))
                .build();
        SmimeaRecordDataDom sha256Long = SmimeaRecordDataDom.builder()
                .usage(1)
                .selector(2)
                .matchingType(1)
                .associationData(sequence(33))
                .build();
        SmimeaRecordDataDom sha512Short = SmimeaRecordDataDom.builder()
                .usage(1)
                .selector(2)
                .matchingType(2)
                .associationData(sequence(63))
                .build();
        SmimeaRecordDataDom sha512Long = SmimeaRecordDataDom.builder()
                .usage(1)
                .selector(2)
                .matchingType(2)
                .associationData(sequence(65))
                .build();

        assertThrows(IllegalArgumentException.class, sha256Short::to);
        assertThrows(IllegalArgumentException.class, sha256Long::to);
        assertThrows(IllegalArgumentException.class, sha512Short::to);
        assertThrows(IllegalArgumentException.class, sha512Long::to);
    }

    @Test
    void toSerializesRdata() {
        byte[] data = sequence(32);
        SmimeaRecordDataDom dom = SmimeaRecordDataDom.builder()
                .usage(1)
                .selector(2)
                .matchingType(1)
                .associationData(data)
                .build();

        byte[] expected = rdataWithData(1, data);
        assertArrayEquals(expected, dom.to());
    }

    @Test
    void roundTripPreservesFields() {
        byte[] data = sequence(32);
        SmimeaRecordDataDom original = SmimeaRecordDataDom.builder()
                .usage(1)
                .selector(2)
                .matchingType(1)
                .associationData(data)
                .build();

        RDataDom decoded = SmimeaRecordDataDom.from(original.to());
        SmimeaRecordDataDom parsed = assertInstanceOf(SmimeaRecordDataDom.class, decoded);

        assertEquals(1, parsed.usage());
        assertEquals(2, parsed.selector());
        assertEquals(1, parsed.matchingType());
        assertArrayEquals(data, parsed.associationData());
    }

    private static byte[] sequence(int length) {
        byte[] out = new byte[length];
        for (int i = 0; i < length; i++) {
            out[i] = (byte) (i + 1);
        }
        return out;
    }

    private static byte[] rdataWithLength(int matchingType, int length) {
        return rdataWithData(matchingType, sequence(length));
    }

    private static byte[] rdataWithData(int matchingType, byte[] data) {
        byte[] rdata = new byte[3 + data.length];
        rdata[0] = 1;
        rdata[1] = 2;
        rdata[2] = (byte) (matchingType & 0xFF);
        System.arraycopy(data, 0, rdata, 3, data.length);
        return rdata;
    }
}
