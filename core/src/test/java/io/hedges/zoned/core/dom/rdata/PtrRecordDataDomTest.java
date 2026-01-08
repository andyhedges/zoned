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

class PtrRecordDataDomTest {
    private static final NameResolver RESOLVER =
            offset -> DnsNameDom.labels(List.of("ptr", "example"));

    @Test
    void fromRejectsInvalidName() {
        assertThrows(IllegalArgumentException.class, () -> PtrRecordDataDom.from(null, null));
        assertThrows(IllegalArgumentException.class, () -> PtrRecordDataDom.from(new byte[0], null));
    }

    @Test
    void fromParsesName() {
        byte[] rdata = new byte[] {3, 'p', 't', 'r', 7, 'e', 'x', 'a', 'm', 'p', 'l', 'e', 0};

        RDataDom dom = PtrRecordDataDom.from(rdata, null);
        PtrRecordDataDom ptr = assertInstanceOf(PtrRecordDataDom.class, dom);

        assertEquals(List.of("ptr", "example"), ptr.ptrName().labelStrings());
    }

    @Test
    void fromUsesResolverForCompressedNames() {
        byte[] rdata = new byte[] {(byte) 0xC0, 0x10};

        RDataDom dom = PtrRecordDataDom.from(rdata, RESOLVER);
        PtrRecordDataDom ptr = assertInstanceOf(PtrRecordDataDom.class, dom);

        assertEquals(List.of("ptr", "example"), ptr.ptrName().labelStrings());
    }

    @Test
    void toRejectsNullPtrName() {
        PtrRecordDataDom dom = PtrRecordDataDom.builder().build();
        assertThrows(IllegalArgumentException.class, dom::to);
    }

    @Test
    void toSerializesName() {
        DnsNameDom name = DnsNameDom.labels(List.of("ptr", "example"));
        PtrRecordDataDom dom = PtrRecordDataDom.builder().ptrName(name).build();

        byte[] encoded = dom.to();

        assertArrayEquals(new byte[] {3, 'p', 't', 'r', 7, 'e', 'x', 'a', 'm', 'p', 'l', 'e', 0}, encoded);
    }

    @Test
    void roundTripPreservesName() {
        DnsNameDom name = DnsNameDom.labels(List.of("host", "example"));
        PtrRecordDataDom original = PtrRecordDataDom.builder().ptrName(name).build();

        RDataDom decoded = PtrRecordDataDom.from(original.to(), RESOLVER);
        PtrRecordDataDom parsed = assertInstanceOf(PtrRecordDataDom.class, decoded);

        assertEquals(List.of("host", "example"), parsed.ptrName().labelStrings());
    }
}
