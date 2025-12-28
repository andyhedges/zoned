// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TxtRecordDataDomTest {

    @Test
    void fromRejectsNullOrShortInput() {
        assertThrows(IllegalArgumentException.class, () -> TxtRecordDataDom.from(null));
        assertThrows(IllegalArgumentException.class, () -> TxtRecordDataDom.from(new byte[0]));
        assertThrows(IllegalArgumentException.class, () -> TxtRecordDataDom.from(new byte[1]));
        assertThrows(IllegalArgumentException.class, () -> TxtRecordDataDom.from(new byte[2]));
    }

    @Test
    void fromRejectsOversizedRdata() {
        assertThrows(IllegalArgumentException.class, () -> TxtRecordDataDom.from(new byte[0x10000]));
    }

    @Test
    void fromRejectsTruncatedCharacterString() {
        byte[] rdata = new byte[] {3, 1, 2};
        assertThrows(IllegalArgumentException.class, () -> TxtRecordDataDom.from(rdata));
    }

    @Test
    void fromParsesCharacterStrings() {
        // 0-length is a valid TXT character-string (length byte), not a terminator.
        //                                           v
        byte[] rdata = new byte[] {3, 'f', 'o', 'o', 0, 3, 'b', 'a', 'r'};
        RDataDom dom = TxtRecordDataDom.from(rdata);

        TxtRecordDataDom txt = assertInstanceOf(TxtRecordDataDom.class, dom);
        List<byte[]> strings = txt.characterStrings();

        assertEquals(3, strings.size());
        assertArrayEquals(new byte[] {'f', 'o', 'o'}, strings.get(0));
        assertArrayEquals(new byte[0], strings.get(1));
        assertArrayEquals(new byte[] {'b', 'a', 'r'}, strings.get(2));
    }

    @Test
    void toRejectsMissingCharacterStrings() {
        TxtRecordDataDom empty = TxtRecordDataDom.builder().build();
        assertThrows(IllegalArgumentException.class, empty::to);

        TxtRecordDataDom none = TxtRecordDataDom.builder().characterStrings(List.of()).build();
        assertThrows(IllegalArgumentException.class, none::to);
    }

    @Test
    void toRejectsNullCharacterString() {
        List<byte[]> strings = new ArrayList<>();
        strings.add(new byte[] {1});
        strings.add(null);
        TxtRecordDataDom dom = TxtRecordDataDom.builder()
                .characterStrings(strings)
                .build();
        assertThrows(IllegalArgumentException.class, dom::to);
    }

    @Test
    void toRejectsCharacterStringLongerThan255Bytes() {
        TxtRecordDataDom dom = TxtRecordDataDom.builder()
                .characterStrings(List.of(new byte[256]))
                .build();
        assertThrows(IllegalArgumentException.class, dom::to);
    }

    @Test
    void toRejectsOversizedRdata() {
        List<byte[]> strings = new ArrayList<>();
        for (int i = 0; i < 256; i++) {
            strings.add(new byte[255]);
        }

        TxtRecordDataDom dom = TxtRecordDataDom.builder()
                .characterStrings(strings)
                .build();
        assertThrows(IllegalArgumentException.class, dom::to);
    }

    @Test
    void toSerializesCharacterStrings() {
        TxtRecordDataDom dom = TxtRecordDataDom.builder()
                .characterStrings(List.of(new byte[] {1, 2}, new byte[] {3}))
                .build();

        byte[] encoded = dom.to();

        assertArrayEquals(new byte[] {2, 1, 2, 1, 3}, encoded);
    }

    @Test
    void roundTripPreservesCharacterStrings() {
        List<byte[]> strings = List.of(
                new byte[] {'a', 'b'},
                new byte[0],
                new byte[] {'z'}
        );

        TxtRecordDataDom original = TxtRecordDataDom.builder()
                .characterStrings(strings)
                .build();

        RDataDom decoded = TxtRecordDataDom.from(original.to());
        TxtRecordDataDom txt = assertInstanceOf(TxtRecordDataDom.class, decoded);

        assertEquals(strings.size(), txt.characterStrings().size());
        for (int i = 0; i < strings.size(); i++) {
            assertArrayEquals(strings.get(i), txt.characterStrings().get(i));
        }
    }
}
