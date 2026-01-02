// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UriRecordDataDomTest {

    @Test
    void fromRejectsShortInput() {
        assertThrows(IllegalArgumentException.class, () -> UriRecordDataDom.from(null));
        assertThrows(IllegalArgumentException.class, () -> UriRecordDataDom.from(new byte[0]));
        assertThrows(IllegalArgumentException.class, () -> UriRecordDataDom.from(new byte[1]));
        assertThrows(IllegalArgumentException.class, () -> UriRecordDataDom.from(new byte[2]));
        assertThrows(IllegalArgumentException.class, () -> UriRecordDataDom.from(new byte[3]));
    }

    @Test
    void fromParsesFields() {
        byte[] targetBytes = "https://example.test/service".getBytes(StandardCharsets.UTF_8);
        byte[] rdata = new byte[4 + targetBytes.length];
        rdata[0] = 0;
        rdata[1] = 10;
        rdata[2] = 0;
        rdata[3] = 20;
        System.arraycopy(targetBytes, 0, rdata, 4, targetBytes.length);

        RDataDom dom = UriRecordDataDom.from(rdata);
        UriRecordDataDom uri = assertInstanceOf(UriRecordDataDom.class, dom);

        assertEquals(10, uri.priority());
        assertEquals(20, uri.weight());
        assertEquals("https://example.test/service", uri.target());
    }

    @Test
    void fromAcceptsEmptyTarget() {
        byte[] rdata = new byte[] {0, 1, 0, 2};
        UriRecordDataDom uri = assertInstanceOf(UriRecordDataDom.class, UriRecordDataDom.from(rdata));

        assertEquals(1, uri.priority());
        assertEquals(2, uri.weight());
        assertEquals("", uri.target());
    }

    @Test
    void toRejectsNullTarget() {
        UriRecordDataDom dom = UriRecordDataDom.builder()
                .priority(1)
                .weight(2)
                .build();
        assertThrows(IllegalArgumentException.class, dom::to);
    }

    @Test
    void toRejectsInvalidPriority() {
        UriRecordDataDom negative = UriRecordDataDom.builder()
                .priority(-1)
                .weight(1)
                .target("https://example.test")
                .build();
        UriRecordDataDom tooLarge = UriRecordDataDom.builder()
                .priority(0x1_0000)
                .weight(1)
                .target("https://example.test")
                .build();

        assertThrows(IllegalArgumentException.class, negative::to);
        assertThrows(IllegalArgumentException.class, tooLarge::to);
    }

    @Test
    void toRejectsInvalidWeight() {
        UriRecordDataDom negative = UriRecordDataDom.builder()
                .priority(1)
                .weight(-1)
                .target("https://example.test")
                .build();
        UriRecordDataDom tooLarge = UriRecordDataDom.builder()
                .priority(1)
                .weight(0x1_0000)
                .target("https://example.test")
                .build();

        assertThrows(IllegalArgumentException.class, negative::to);
        assertThrows(IllegalArgumentException.class, tooLarge::to);
    }

    @Test
    void toRejectsOversizedTarget() {
        String target = "a".repeat(256);
        UriRecordDataDom dom = UriRecordDataDom.builder()
                .priority(1)
                .weight(2)
                .target(target)
                .build();

        assertThrows(IllegalArgumentException.class, dom::to);
    }

    @Test
    void toSerializesRdata() {
        String target = "https://example.test/service";
        UriRecordDataDom dom = UriRecordDataDom.builder()
                .priority(10)
                .weight(20)
                .target(target)
                .build();

        byte[] targetBytes = target.getBytes(StandardCharsets.UTF_8);
        byte[] expected = new byte[4 + targetBytes.length];
        expected[0] = 0;
        expected[1] = 10;
        expected[2] = 0;
        expected[3] = 20;
        System.arraycopy(targetBytes, 0, expected, 4, targetBytes.length);

        assertArrayEquals(expected, dom.to());
    }

    @Test
    void boundaryValuesRoundTrip() {
        String target = "a".repeat(255);
        UriRecordDataDom dom = UriRecordDataDom.builder()
                .priority(0)
                .weight(0xFFFF)
                .target(target)
                .build();

        UriRecordDataDom decoded = assertInstanceOf(UriRecordDataDom.class, UriRecordDataDom.from(dom.to()));

        assertEquals(0, decoded.priority());
        assertEquals(0xFFFF, decoded.weight());
        assertEquals(target, decoded.target());
    }
}
