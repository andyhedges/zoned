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

class KxRecordDataDomTest {
    private static final NameResolver RESOLVER =
            offset -> DnsNameDom.builder().labels(List.of("kx", "example")).build();

    @Test
    void fromRejectsInvalidLength() {
        assertThrows(IllegalArgumentException.class, () -> KxRecordDataDom.from(null, RESOLVER));
        assertThrows(IllegalArgumentException.class, () -> KxRecordDataDom.from(new byte[0], RESOLVER));
        assertThrows(IllegalArgumentException.class, () -> KxRecordDataDom.from(new byte[2], RESOLVER));
    }

    @Test
    void fromParsesFields() {
        byte[] rdata = new byte[] {0, 10, 2, 'k', 'x', 7, 'e', 'x', 'a', 'm', 'p', 'l', 'e', 4, 't', 'e', 's', 't', 0};

        RDataDom dom = KxRecordDataDom.from(rdata, RESOLVER);
        KxRecordDataDom kx = assertInstanceOf(KxRecordDataDom.class, dom);

        assertEquals(10, kx.preference());
        assertEquals(List.of("kx", "example", "test"), kx.exchanger().labels());
    }

    @Test
    void fromUsesResolverForCompressedNames() {
        byte[] rdata = new byte[] {0, 5, (byte) 0xC0, 0x10};

        RDataDom dom = KxRecordDataDom.from(rdata, RESOLVER);
        KxRecordDataDom kx = assertInstanceOf(KxRecordDataDom.class, dom);

        assertEquals(5, kx.preference());
        assertEquals(List.of("kx", "example"), kx.exchanger().labels());
    }

    @Test
    void toRejectsInvalidFields() {
        KxRecordDataDom missingName = KxRecordDataDom.builder().preference(10).build();
        KxRecordDataDom negativePreference = KxRecordDataDom.builder()
                .preference(-1)
                .exchanger(DnsNameDom.builder().labels(List.of("kx", "example")).build())
                .build();
        KxRecordDataDom tooLargePreference = KxRecordDataDom.builder()
                .preference(0x1_0000)
                .exchanger(DnsNameDom.builder().labels(List.of("kx", "example")).build())
                .build();

        assertThrows(IllegalArgumentException.class, missingName::to);
        assertThrows(IllegalArgumentException.class, negativePreference::to);
        assertThrows(IllegalArgumentException.class, tooLargePreference::to);
    }

    @Test
    void toSerializesRdata() {
        DnsNameDom name = DnsNameDom.builder().labels(List.of("kx", "example", "test")).build();
        KxRecordDataDom dom = KxRecordDataDom.builder().preference(25).exchanger(name).build();

        byte[] expected = new byte[] {0, 25, 2, 'k', 'x', 7, 'e', 'x', 'a', 'm', 'p', 'l', 'e', 4, 't', 'e', 's', 't', 0};
        assertArrayEquals(expected, dom.to());
    }

    @Test
    void roundTripPreservesFields() {
        DnsNameDom name = DnsNameDom.builder().labels(List.of("kx", "example", "test")).build();
        KxRecordDataDom original = KxRecordDataDom.builder().preference(15).exchanger(name).build();

        RDataDom decoded = KxRecordDataDom.from(original.to(), RESOLVER);
        KxRecordDataDom parsed = assertInstanceOf(KxRecordDataDom.class, decoded);

        assertEquals(15, parsed.preference());
        assertEquals(List.of("kx", "example", "test"), parsed.exchanger().labels());
    }
}
