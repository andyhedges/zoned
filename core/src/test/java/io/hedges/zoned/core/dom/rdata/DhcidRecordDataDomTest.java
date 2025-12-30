// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DhcidRecordDataDomTest {

    @Test
    void fromRejectsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> DhcidRecordDataDom.from(null));
        assertThrows(IllegalArgumentException.class, () -> DhcidRecordDataDom.from(new byte[0]));
    }

    @Test
    void fromCopiesIdentifier() {
        byte[] rdata = new byte[] {1, 2, 3};
        RDataDom dom = DhcidRecordDataDom.from(rdata);
        DhcidRecordDataDom dhcid = assertInstanceOf(DhcidRecordDataDom.class, dom);

        assertArrayEquals(rdata, dhcid.identifier());
    }

    @Test
    void toRejectsMissingIdentifier() {
        DhcidRecordDataDom missing = DhcidRecordDataDom.builder().build();
        DhcidRecordDataDom empty = DhcidRecordDataDom.builder().identifier(new byte[0]).build();

        assertThrows(IllegalArgumentException.class, missing::to);
        assertThrows(IllegalArgumentException.class, empty::to);
    }

    @Test
    void toSerializesIdentifier() {
        byte[] identifier = new byte[] {4, 5, 6};
        DhcidRecordDataDom dom = DhcidRecordDataDom.builder().identifier(identifier).build();

        assertArrayEquals(identifier, dom.to());
    }

    @Test
    void roundTripPreservesIdentifier() {
        byte[] identifier = new byte[] {9, 8, 7, 6};
        DhcidRecordDataDom original = DhcidRecordDataDom.builder().identifier(identifier).build();
        RDataDom decoded = DhcidRecordDataDom.from(original.to());
        DhcidRecordDataDom parsed = assertInstanceOf(DhcidRecordDataDom.class, decoded);

        assertArrayEquals(identifier, parsed.identifier());
    }
}
