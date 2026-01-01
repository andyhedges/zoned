// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TlsaRecordDataDomTest {

    @Test
    void fromRejectsInvalidLength() {
        assertThrows(IllegalArgumentException.class, () -> TlsaRecordDataDom.from(null));
        assertThrows(IllegalArgumentException.class, () -> TlsaRecordDataDom.from(new byte[3]));
    }

    @Test
    void fromRejectsEmptyData() {
        byte[] rdata = new byte[] {1, 2, 3};
        assertThrows(IllegalArgumentException.class, () -> TlsaRecordDataDom.from(rdata));
    }

    @Test
    void fromRejectsInvalidAssociationLength() {
        assertThrows(IllegalArgumentException.class, () -> TlsaRecordDataDom.from(rdataWithLength(1, 31)));
        assertThrows(IllegalArgumentException.class, () -> TlsaRecordDataDom.from(rdataWithLength(1, 33)));
        assertThrows(IllegalArgumentException.class, () -> TlsaRecordDataDom.from(rdataWithLength(2, 63)));
        assertThrows(IllegalArgumentException.class, () -> TlsaRecordDataDom.from(rdataWithLength(2, 65)));
    }

    @Test
    void fromParsesFields() {
        byte[] data = sequence(32);
        byte[] rdata = rdataWithData(1, data);
        RDataDom dom = TlsaRecordDataDom.from(rdata);
        TlsaRecordDataDom tlsa = assertInstanceOf(TlsaRecordDataDom.class, dom);

        assertEquals(1, tlsa.usage());
        assertEquals(2, tlsa.selector());
        assertEquals(1, tlsa.matchingType());
        assertArrayEquals(data, tlsa.associationData());
    }

    @Test
    void toRejectsInvalidFields() {
        TlsaRecordDataDom missingData = TlsaRecordDataDom.builder()
                .usage(1)
                .selector(2)
                .matchingType(1)
                .build();
        TlsaRecordDataDom emptyData = TlsaRecordDataDom.builder()
                .usage(1)
                .selector(2)
                .matchingType(1)
                .associationData(new byte[0])
                .build();
        TlsaRecordDataDom negativeUsage = TlsaRecordDataDom.builder()
                .usage(-1)
                .selector(2)
                .matchingType(1)
                .associationData(sequence(32))
                .build();
        TlsaRecordDataDom tooLargeUsage = TlsaRecordDataDom.builder()
                .usage(256)
                .selector(2)
                .matchingType(1)
                .associationData(sequence(32))
                .build();
        TlsaRecordDataDom negativeSelector = TlsaRecordDataDom.builder()
                .usage(1)
                .selector(-1)
                .matchingType(1)
                .associationData(sequence(32))
                .build();
        TlsaRecordDataDom tooLargeSelector = TlsaRecordDataDom.builder()
                .usage(1)
                .selector(256)
                .matchingType(1)
                .associationData(sequence(32))
                .build();
        TlsaRecordDataDom negativeMatching = TlsaRecordDataDom.builder()
                .usage(1)
                .selector(2)
                .matchingType(-1)
                .associationData(sequence(32))
                .build();
        TlsaRecordDataDom tooLargeMatching = TlsaRecordDataDom.builder()
                .usage(1)
                .selector(2)
                .matchingType(256)
                .associationData(sequence(32))
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
        TlsaRecordDataDom sha256Short = TlsaRecordDataDom.builder()
                .usage(1)
                .selector(2)
                .matchingType(1)
                .associationData(sequence(31))
                .build();
        TlsaRecordDataDom sha256Long = TlsaRecordDataDom.builder()
                .usage(1)
                .selector(2)
                .matchingType(1)
                .associationData(sequence(33))
                .build();
        TlsaRecordDataDom sha512Short = TlsaRecordDataDom.builder()
                .usage(1)
                .selector(2)
                .matchingType(2)
                .associationData(sequence(63))
                .build();
        TlsaRecordDataDom sha512Long = TlsaRecordDataDom.builder()
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
        TlsaRecordDataDom dom = TlsaRecordDataDom.builder()
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
        TlsaRecordDataDom original = TlsaRecordDataDom.builder()
                .usage(1)
                .selector(2)
                .matchingType(1)
                .associationData(data)
                .build();

        RDataDom decoded = TlsaRecordDataDom.from(original.to());
        TlsaRecordDataDom parsed = assertInstanceOf(TlsaRecordDataDom.class, decoded);

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
