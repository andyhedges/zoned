// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.NameResolver;
import io.hedges.zoned.core.dom.DnsNameDom;
import io.hedges.zoned.core.dom.RDataDom;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TkeyRecordDataDomTest {
    private static final NameResolver RESOLVER =
            offset -> DnsNameDom.builder().labels(List.of("alg", "example")).build();

    @Test
    void fromRejectsInvalidLength() {
        assertThrows(IllegalArgumentException.class, () -> TkeyRecordDataDom.from(null, RESOLVER));
        assertThrows(IllegalArgumentException.class, () -> TkeyRecordDataDom.from(new byte[0], RESOLVER));
        byte[] nameOnly = nameBytes();
        assertThrows(IllegalArgumentException.class, () -> TkeyRecordDataDom.from(nameOnly, RESOLVER));
        byte[] tooShort = new byte[nameOnly.length + 15];
        System.arraycopy(nameOnly, 0, tooShort, 0, nameOnly.length);
        assertThrows(IllegalArgumentException.class, () -> TkeyRecordDataDom.from(tooShort, RESOLVER));
    }

    @Test
    void fromRejectsKeyLengthOutOfBounds() {
        byte[] nameBytes = nameBytes();
        byte[] rdata = new byte[nameBytes.length + 19];
        int idx = 0;
        System.arraycopy(nameBytes, 0, rdata, idx, nameBytes.length);
        idx += nameBytes.length;
        idx = writeU32(rdata, idx, 1);
        idx = writeU32(rdata, idx, 2);
        idx = writeU16(rdata, idx, 3);
        idx = writeU16(rdata, idx, 0);
        idx = writeU16(rdata, idx, 4);
        rdata[idx++] = 1;
        rdata[idx++] = 2;
        rdata[idx++] = 3;
        idx = writeU16(rdata, idx, 0);

        assertThrows(IllegalArgumentException.class, () -> TkeyRecordDataDom.from(rdata, RESOLVER));
    }

    @Test
    void fromRejectsOtherLengthOutOfBounds() {
        byte[] nameBytes = nameBytes();
        byte[] rdata = new byte[nameBytes.length + 19];
        int idx = 0;
        System.arraycopy(nameBytes, 0, rdata, idx, nameBytes.length);
        idx += nameBytes.length;
        idx = writeU32(rdata, idx, 1);
        idx = writeU32(rdata, idx, 2);
        idx = writeU16(rdata, idx, 3);
        idx = writeU16(rdata, idx, 0);
        idx = writeU16(rdata, idx, 0);
        idx = writeU16(rdata, idx, 4);
        rdata[idx++] = 0x01;
        rdata[idx++] = 0x02;
        rdata[idx++] = 0x03;

        assertThrows(IllegalArgumentException.class, () -> TkeyRecordDataDom.from(rdata, RESOLVER));
    }

    @Test
    void fromRejectsExtraBytes() {
        byte[] nameBytes = nameBytes();
        byte[] rdata = new byte[nameBytes.length + 18];
        int idx = 0;
        System.arraycopy(nameBytes, 0, rdata, idx, nameBytes.length);
        idx += nameBytes.length;
        idx = writeU32(rdata, idx, 1);
        idx = writeU32(rdata, idx, 2);
        idx = writeU16(rdata, idx, 3);
        idx = writeU16(rdata, idx, 0);
        idx = writeU16(rdata, idx, 0);
        idx = writeU16(rdata, idx, 1);
        rdata[idx++] = 0x01;
        rdata[idx++] = 0x02;

        assertThrows(IllegalArgumentException.class, () -> TkeyRecordDataDom.from(rdata, RESOLVER));
    }

    @Test
    void fromParsesFields() {
        byte[] nameBytes = nameBytes();
        byte[] keyData = new byte[] {1, 2, 3, 4};
        byte[] otherData = new byte[] {(byte) 0xaa, (byte) 0xbb};
        byte[] rdata = new byte[nameBytes.length + 16 + keyData.length + otherData.length];
        int idx = 0;
        System.arraycopy(nameBytes, 0, rdata, idx, nameBytes.length);
        idx += nameBytes.length;
        idx = writeU32(rdata, idx, 1);
        idx = writeU32(rdata, idx, 2);
        idx = writeU16(rdata, idx, 3);
        idx = writeU16(rdata, idx, 4);
        idx = writeU16(rdata, idx, keyData.length);
        System.arraycopy(keyData, 0, rdata, idx, keyData.length);
        idx += keyData.length;
        idx = writeU16(rdata, idx, otherData.length);
        System.arraycopy(otherData, 0, rdata, idx, otherData.length);

        RDataDom dom = TkeyRecordDataDom.from(rdata, RESOLVER);
        TkeyRecordDataDom tkey = assertInstanceOf(TkeyRecordDataDom.class, dom);

        assertEquals(List.of("alg", "example", "test"), tkey.algorithm().labels());
        assertEquals(1, tkey.inception());
        assertEquals(2, tkey.expiration());
        assertEquals(3, tkey.mode());
        assertEquals(4, tkey.error());
        assertArrayEquals(keyData, tkey.keyData());
        assertArrayEquals(otherData, tkey.otherData());
    }

    @Test
    void fromUsesResolverForCompressedNames() {
        byte[] rdata = new byte[] {
                (byte) 0xC0, 0x10,
                0, 0, 0, 1,
                0, 0, 0, 2,
                0, 3,
                0, 4,
                0, 0,
                0, 0
        };

        RDataDom dom = TkeyRecordDataDom.from(rdata, RESOLVER);
        TkeyRecordDataDom tkey = assertInstanceOf(TkeyRecordDataDom.class, dom);

        assertEquals(List.of("alg", "example"), tkey.algorithm().labels());
        assertEquals(1, tkey.inception());
        assertEquals(2, tkey.expiration());
        assertEquals(3, tkey.mode());
        assertEquals(4, tkey.error());
        assertArrayEquals(new byte[0], tkey.keyData());
        assertArrayEquals(new byte[0], tkey.otherData());
    }

    @Test
    void toRejectsInvalidFields() {
        TkeyRecordDataDom missingAlgorithm = TkeyRecordDataDom.builder()
                .inception(0)
                .expiration(0)
                .mode(0)
                .error(0)
                .keyData(new byte[0])
                .otherData(new byte[0])
                .build();
        TkeyRecordDataDom negativeInception = TkeyRecordDataDom.builder()
                .algorithm(nameDom())
                .inception(-1)
                .expiration(0)
                .mode(0)
                .error(0)
                .keyData(new byte[0])
                .otherData(new byte[0])
                .build();
        TkeyRecordDataDom tooLargeInception = TkeyRecordDataDom.builder()
                .algorithm(nameDom())
                .inception(0x1_0000_0000L)
                .expiration(0)
                .mode(0)
                .error(0)
                .keyData(new byte[0])
                .otherData(new byte[0])
                .build();
        TkeyRecordDataDom negativeExpiration = TkeyRecordDataDom.builder()
                .algorithm(nameDom())
                .inception(0)
                .expiration(-1)
                .mode(0)
                .error(0)
                .keyData(new byte[0])
                .otherData(new byte[0])
                .build();
        TkeyRecordDataDom tooLargeExpiration = TkeyRecordDataDom.builder()
                .algorithm(nameDom())
                .inception(0)
                .expiration(0x1_0000_0000L)
                .mode(0)
                .error(0)
                .keyData(new byte[0])
                .otherData(new byte[0])
                .build();
        TkeyRecordDataDom negativeMode = TkeyRecordDataDom.builder()
                .algorithm(nameDom())
                .inception(0)
                .expiration(0)
                .mode(-1)
                .error(0)
                .keyData(new byte[0])
                .otherData(new byte[0])
                .build();
        TkeyRecordDataDom tooLargeMode = TkeyRecordDataDom.builder()
                .algorithm(nameDom())
                .inception(0)
                .expiration(0)
                .mode(0x1_0000)
                .error(0)
                .keyData(new byte[0])
                .otherData(new byte[0])
                .build();
        TkeyRecordDataDom negativeError = TkeyRecordDataDom.builder()
                .algorithm(nameDom())
                .inception(0)
                .expiration(0)
                .mode(0)
                .error(-1)
                .keyData(new byte[0])
                .otherData(new byte[0])
                .build();
        TkeyRecordDataDom tooLargeError = TkeyRecordDataDom.builder()
                .algorithm(nameDom())
                .inception(0)
                .expiration(0)
                .mode(0)
                .error(0x1_0000)
                .keyData(new byte[0])
                .otherData(new byte[0])
                .build();
        TkeyRecordDataDom nullKeyData = TkeyRecordDataDom.builder()
                .algorithm(nameDom())
                .inception(0)
                .expiration(0)
                .mode(0)
                .error(0)
                .otherData(new byte[0])
                .build();
        TkeyRecordDataDom nullOtherData = TkeyRecordDataDom.builder()
                .algorithm(nameDom())
                .inception(0)
                .expiration(0)
                .mode(0)
                .error(0)
                .keyData(new byte[0])
                .build();
        TkeyRecordDataDom oversizedKeyData = TkeyRecordDataDom.builder()
                .algorithm(nameDom())
                .inception(0)
                .expiration(0)
                .mode(0)
                .error(0)
                .keyData(new byte[0x1_0000])
                .otherData(new byte[0])
                .build();
        TkeyRecordDataDom oversizedOtherData = TkeyRecordDataDom.builder()
                .algorithm(nameDom())
                .inception(0)
                .expiration(0)
                .mode(0)
                .error(0)
                .keyData(new byte[0])
                .otherData(new byte[0x1_0000])
                .build();

        assertThrows(IllegalArgumentException.class, missingAlgorithm::to);
        assertThrows(IllegalArgumentException.class, negativeInception::to);
        assertThrows(IllegalArgumentException.class, tooLargeInception::to);
        assertThrows(IllegalArgumentException.class, negativeExpiration::to);
        assertThrows(IllegalArgumentException.class, tooLargeExpiration::to);
        assertThrows(IllegalArgumentException.class, negativeMode::to);
        assertThrows(IllegalArgumentException.class, tooLargeMode::to);
        assertThrows(IllegalArgumentException.class, negativeError::to);
        assertThrows(IllegalArgumentException.class, tooLargeError::to);
        assertThrows(IllegalArgumentException.class, nullKeyData::to);
        assertThrows(IllegalArgumentException.class, nullOtherData::to);
        assertThrows(IllegalArgumentException.class, oversizedKeyData::to);
        assertThrows(IllegalArgumentException.class, oversizedOtherData::to);
    }

    @Test
    void toSerializesRdata() {
        DnsNameDom algorithm = nameDom();
        byte[] keyData = new byte[] {1, 2, 3, 4};
        byte[] otherData = new byte[] {(byte) 0xaa, (byte) 0xbb};
        TkeyRecordDataDom dom = TkeyRecordDataDom.builder()
                .algorithm(algorithm)
                .inception(1)
                .expiration(2)
                .mode(3)
                .error(4)
                .keyData(keyData)
                .otherData(otherData)
                .build();

        byte[] nameBytes = nameBytes();
        byte[] expected = new byte[nameBytes.length + 16 + keyData.length + otherData.length];
        int idx = 0;
        System.arraycopy(nameBytes, 0, expected, idx, nameBytes.length);
        idx += nameBytes.length;
        idx = writeU32(expected, idx, 1);
        idx = writeU32(expected, idx, 2);
        idx = writeU16(expected, idx, 3);
        idx = writeU16(expected, idx, 4);
        idx = writeU16(expected, idx, keyData.length);
        System.arraycopy(keyData, 0, expected, idx, keyData.length);
        idx += keyData.length;
        idx = writeU16(expected, idx, otherData.length);
        System.arraycopy(otherData, 0, expected, idx, otherData.length);

        assertArrayEquals(expected, dom.to());
    }

    @Test
    void roundTripPreservesFields() {
        byte[] keyData = new byte[] {9, 8};
        byte[] otherData = new byte[] {7};
        TkeyRecordDataDom original = TkeyRecordDataDom.builder()
                .algorithm(nameDom())
                .inception(5)
                .expiration(6)
                .mode(7)
                .error(8)
                .keyData(keyData)
                .otherData(otherData)
                .build();

        RDataDom decoded = TkeyRecordDataDom.from(original.to(), RESOLVER);
        TkeyRecordDataDom parsed = assertInstanceOf(TkeyRecordDataDom.class, decoded);

        assertEquals(5, parsed.inception());
        assertEquals(6, parsed.expiration());
        assertEquals(7, parsed.mode());
        assertEquals(8, parsed.error());
        assertArrayEquals(keyData, parsed.keyData());
        assertArrayEquals(otherData, parsed.otherData());
    }

    private static DnsNameDom nameDom() {
        return DnsNameDom.builder().labels(List.of("alg", "example", "test")).build();
    }

    private static byte[] nameBytes() {
        return RDataUtils.toByteArray(nameDom());
    }

    private static int writeU16(byte[] out, int offset, int value) {
        out[offset] = (byte) ((value >> 8) & 0xFF);
        out[offset + 1] = (byte) (value & 0xFF);
        return offset + 2;
    }

    private static int writeU32(byte[] out, int offset, long value) {
        out[offset] = (byte) ((value >> 24) & 0xFF);
        out[offset + 1] = (byte) ((value >> 16) & 0xFF);
        out[offset + 2] = (byte) ((value >> 8) & 0xFF);
        out[offset + 3] = (byte) (value & 0xFF);
        return offset + 4;
    }
}
