// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Eui48RecordDataDomTest {

    @Test
    void fromRejectsInvalidLength() {
        assertThrows(IllegalArgumentException.class, () -> Eui48RecordDataDom.from(null));
        assertThrows(IllegalArgumentException.class, () -> Eui48RecordDataDom.from(new byte[5]));
        assertThrows(IllegalArgumentException.class, () -> Eui48RecordDataDom.from(new byte[7]));
    }

    @Test
    void fromParsesAddress() {
        byte[] rdata = new byte[] {0, 1, 2, 3, 4, 5};
        RDataDom dom = Eui48RecordDataDom.from(rdata);
        Eui48RecordDataDom eui48 = assertInstanceOf(Eui48RecordDataDom.class, dom);

        assertArrayEquals(rdata, eui48.address());
    }

    @Test
    void toRejectsInvalidAddress() {
        Eui48RecordDataDom missing = Eui48RecordDataDom.builder().build();
        Eui48RecordDataDom shortAddress = Eui48RecordDataDom.builder().address(new byte[5]).build();
        Eui48RecordDataDom longAddress = Eui48RecordDataDom.builder().address(new byte[7]).build();

        assertThrows(IllegalArgumentException.class, missing::to);
        assertThrows(IllegalArgumentException.class, shortAddress::to);
        assertThrows(IllegalArgumentException.class, longAddress::to);
    }

    @Test
    void toSerializesAddress() {
        byte[] address = new byte[] {0, 1, 2, 3, 4, 5};
        Eui48RecordDataDom dom = Eui48RecordDataDom.builder().address(address).build();

        assertArrayEquals(address, dom.to());
    }

    @Test
    void roundTripPreservesAddress() {
        byte[] address = new byte[] {9, 8, 7, 6, 5, 4};
        Eui48RecordDataDom original = Eui48RecordDataDom.builder().address(address).build();
        RDataDom decoded = Eui48RecordDataDom.from(original.to());
        Eui48RecordDataDom parsed = assertInstanceOf(Eui48RecordDataDom.class, decoded);

        assertArrayEquals(address, parsed.address());
    }
}
