// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocRecordDataDomTest {

    @Test
    void fromRejectsInvalidLength() {
        assertThrows(IllegalArgumentException.class, () -> LocRecordDataDom.from(null));
        assertThrows(IllegalArgumentException.class, () -> LocRecordDataDom.from(new byte[15]));
        assertThrows(IllegalArgumentException.class, () -> LocRecordDataDom.from(new byte[17]));
    }

    @Test
    void fromRejectsInvalidVersion() {
        byte[] rdata = new byte[16];
        rdata[0] = 1;
        assertThrows(IllegalArgumentException.class, () -> LocRecordDataDom.from(rdata));
    }

    @Test
    void fromParsesFields() {
        byte[] rdata = new byte[16];
        rdata[1] = 0x12;
        rdata[2] = 0x34;
        rdata[3] = 0x56;
        rdata[4] = 0x01;
        rdata[8] = 0x02;
        rdata[12] = 0x03;

        RDataDom dom = LocRecordDataDom.from(rdata);
        LocRecordDataDom loc = assertInstanceOf(LocRecordDataDom.class, dom);

        assertEquals(0, loc.version());
        assertEquals(0x12, loc.size());
        assertEquals(0x34, loc.horizontalPrecision());
        assertEquals(0x56, loc.verticalPrecision());
        assertEquals(0x01000000L, loc.latitude());
        assertEquals(0x02000000L, loc.longitude());
        assertEquals(0x03000000L, loc.altitude());
    }

    @Test
    void toRejectsInvalidFields() {
        LocRecordDataDom badVersion = LocRecordDataDom.builder()
                .version(1)
                .size(0)
                .horizontalPrecision(0)
                .verticalPrecision(0)
                .latitude(0)
                .longitude(0)
                .altitude(0)
                .build();
        LocRecordDataDom badSize = LocRecordDataDom.builder()
                .version(0)
                .size(256)
                .horizontalPrecision(0)
                .verticalPrecision(0)
                .latitude(0)
                .longitude(0)
                .altitude(0)
                .build();
        LocRecordDataDom badHorizontal = LocRecordDataDom.builder()
                .version(0)
                .size(0)
                .horizontalPrecision(256)
                .verticalPrecision(0)
                .latitude(0)
                .longitude(0)
                .altitude(0)
                .build();
        LocRecordDataDom badVertical = LocRecordDataDom.builder()
                .version(0)
                .size(0)
                .horizontalPrecision(0)
                .verticalPrecision(256)
                .latitude(0)
                .longitude(0)
                .altitude(0)
                .build();
        LocRecordDataDom badLatitude = LocRecordDataDom.builder()
                .version(0)
                .size(0)
                .horizontalPrecision(0)
                .verticalPrecision(0)
                .latitude(-1)
                .longitude(0)
                .altitude(0)
                .build();
        LocRecordDataDom badLongitude = LocRecordDataDom.builder()
                .version(0)
                .size(0)
                .horizontalPrecision(0)
                .verticalPrecision(0)
                .latitude(0)
                .longitude(0x1_0000_0000L)
                .altitude(0)
                .build();
        LocRecordDataDom badAltitude = LocRecordDataDom.builder()
                .version(0)
                .size(0)
                .horizontalPrecision(0)
                .verticalPrecision(0)
                .latitude(0)
                .longitude(0)
                .altitude(-1)
                .build();

        assertThrows(IllegalArgumentException.class, badVersion::to);
        assertThrows(IllegalArgumentException.class, badSize::to);
        assertThrows(IllegalArgumentException.class, badHorizontal::to);
        assertThrows(IllegalArgumentException.class, badVertical::to);
        assertThrows(IllegalArgumentException.class, badLatitude::to);
        assertThrows(IllegalArgumentException.class, badLongitude::to);
        assertThrows(IllegalArgumentException.class, badAltitude::to);
    }

    @Test
    void toSerializesRdata() {
        LocRecordDataDom dom = LocRecordDataDom.builder()
                .version(0)
                .size(0x12)
                .horizontalPrecision(0x34)
                .verticalPrecision(0x56)
                .latitude(0x01000000L)
                .longitude(0x02000000L)
                .altitude(0x03000000L)
                .build();

        byte[] expected = new byte[16];
        expected[1] = 0x12;
        expected[2] = 0x34;
        expected[3] = 0x56;
        expected[4] = 0x01;
        expected[8] = 0x02;
        expected[12] = 0x03;
        assertArrayEquals(expected, dom.to());
    }

    @Test
    void roundTripPreservesFields() {
        LocRecordDataDom original = LocRecordDataDom.builder()
                .version(0)
                .size(0x11)
                .horizontalPrecision(0x22)
                .verticalPrecision(0x33)
                .latitude(0x04030201L)
                .longitude(0x08070605L)
                .altitude(0x0c0b0a09L)
                .build();

        RDataDom decoded = LocRecordDataDom.from(original.to());
        LocRecordDataDom parsed = assertInstanceOf(LocRecordDataDom.class, decoded);

        assertEquals(0, parsed.version());
        assertEquals(0x11, parsed.size());
        assertEquals(0x22, parsed.horizontalPrecision());
        assertEquals(0x33, parsed.verticalPrecision());
        assertEquals(0x04030201L, parsed.latitude());
        assertEquals(0x08070605L, parsed.longitude());
        assertEquals(0x0c0b0a09L, parsed.altitude());
    }
}
