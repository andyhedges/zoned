// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Eui64RecordDataDomTest {

    @Test
    void fromRejectsInvalidLength() {
        assertThrows(IllegalArgumentException.class, () -> Eui64RecordDataDom.from(null));
        assertThrows(IllegalArgumentException.class, () -> Eui64RecordDataDom.from(new byte[7]));
        assertThrows(IllegalArgumentException.class, () -> Eui64RecordDataDom.from(new byte[9]));
    }

    @Test
    void fromParsesAddress() {
        byte[] rdata = new byte[] {0, 1, 2, 3, 4, 5, 6, 7};
        RDataDom dom = Eui64RecordDataDom.from(rdata);
        Eui64RecordDataDom eui64 = assertInstanceOf(Eui64RecordDataDom.class, dom);

        assertArrayEquals(rdata, eui64.address());
    }

    @Test
    void toRejectsInvalidAddress() {
        Eui64RecordDataDom missing = Eui64RecordDataDom.builder().build();
        Eui64RecordDataDom shortAddress = Eui64RecordDataDom.builder().address(new byte[7]).build();
        Eui64RecordDataDom longAddress = Eui64RecordDataDom.builder().address(new byte[9]).build();

        assertThrows(IllegalArgumentException.class, missing::to);
        assertThrows(IllegalArgumentException.class, shortAddress::to);
        assertThrows(IllegalArgumentException.class, longAddress::to);
    }

    @Test
    void toSerializesAddress() {
        byte[] address = new byte[] {0, 1, 2, 3, 4, 5, 6, 7};
        Eui64RecordDataDom dom = Eui64RecordDataDom.builder().address(address).build();

        assertArrayEquals(address, dom.to());
    }

    @Test
    void roundTripPreservesAddress() {
        byte[] address = new byte[] {9, 8, 7, 6, 5, 4, 3, 2};
        Eui64RecordDataDom original = Eui64RecordDataDom.builder().address(address).build();
        RDataDom decoded = Eui64RecordDataDom.from(original.to());
        Eui64RecordDataDom parsed = assertInstanceOf(Eui64RecordDataDom.class, decoded);

        assertArrayEquals(address, parsed.address());
    }
}
