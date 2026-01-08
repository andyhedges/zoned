// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.DnsNameDom;
import io.hedges.zoned.core.dom.RDataDom;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NsecRecordDataDomTest {

    @Test
    void fromRejectsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> NsecRecordDataDom.from(null, null));
        assertThrows(IllegalArgumentException.class, () -> NsecRecordDataDom.from(new byte[0], null));
    }

    @Test
    void fromRejectsMissingTypeBitmaps() {
        byte[] rdata = new byte[] {1, 'a', 0};
        assertThrows(IllegalArgumentException.class, () -> NsecRecordDataDom.from(rdata, null));
    }

    @Test
    void fromParsesFields() {
        DnsNameDom name = DnsNameDom.labels(List.of("next", "example", "test"));
        byte[] nameBytes = RDataUtils.toByteArray(name);
        byte[] typeBitmaps = new byte[] {0, 1, 64};
        byte[] rdata = new byte[nameBytes.length + typeBitmaps.length];
        System.arraycopy(nameBytes, 0, rdata, 0, nameBytes.length);
        System.arraycopy(typeBitmaps, 0, rdata, nameBytes.length, typeBitmaps.length);

        RDataDom dom = NsecRecordDataDom.from(rdata, null);
        NsecRecordDataDom nsec = assertInstanceOf(NsecRecordDataDom.class, dom);

        assertEquals(List.of("next", "example", "test"), nsec.nextName().labelStrings());
        assertArrayEquals(typeBitmaps, nsec.typeBitmaps());
    }

    @Test
    void toRejectsInvalidFields() {
        NsecRecordDataDom missingName = NsecRecordDataDom.builder()
                .typeBitmaps(new byte[] {1})
                .build();
        NsecRecordDataDom missingTypes = NsecRecordDataDom.builder()
                .nextName(DnsNameDom.labels(List.of("next", "example")))
                .build();
        NsecRecordDataDom emptyTypes = NsecRecordDataDom.builder()
                .nextName(DnsNameDom.labels(List.of("next", "example")))
                .typeBitmaps(new byte[0])
                .build();

        assertThrows(IllegalArgumentException.class, missingName::to);
        assertThrows(IllegalArgumentException.class, missingTypes::to);
        assertThrows(IllegalArgumentException.class, emptyTypes::to);
    }

    @Test
    void toSerializesRdata() {
        DnsNameDom name = DnsNameDom.labels(List.of("next", "example", "test"));
        byte[] nameBytes = RDataUtils.toByteArray(name);
        byte[] typeBitmaps = new byte[] {0, 1, 64};
        NsecRecordDataDom dom = NsecRecordDataDom.builder()
                .nextName(name)
                .typeBitmaps(typeBitmaps)
                .build();

        byte[] expected = new byte[nameBytes.length + typeBitmaps.length];
        System.arraycopy(nameBytes, 0, expected, 0, nameBytes.length);
        System.arraycopy(typeBitmaps, 0, expected, nameBytes.length, typeBitmaps.length);

        assertArrayEquals(expected, dom.to());
    }

    @Test
    void roundTripPreservesFields() {
        DnsNameDom name = DnsNameDom.labels(List.of("next", "example", "test"));
        byte[] typeBitmaps = new byte[] {0, 1, 64};
        NsecRecordDataDom original = NsecRecordDataDom.builder()
                .nextName(name)
                .typeBitmaps(typeBitmaps)
                .build();

        RDataDom decoded = NsecRecordDataDom.from(original.to(), null);
        NsecRecordDataDom parsed = assertInstanceOf(NsecRecordDataDom.class, decoded);

        assertEquals(List.of("next", "example", "test"), parsed.nextName().labelStrings());
        assertArrayEquals(typeBitmaps, parsed.typeBitmaps());
    }
}
