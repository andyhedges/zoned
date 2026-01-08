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

class AfsDbRecordDataDomTest {
    private static final NameResolver RESOLVER =
            offset -> DnsNameDom.labels(List.of("afsdb", "example"));

    @Test
    void fromRejectsShortInput() {
        assertThrows(IllegalArgumentException.class, () -> AfsDbRecordDataDom.from(null, RESOLVER));
        assertThrows(IllegalArgumentException.class, () -> AfsDbRecordDataDom.from(new byte[0], RESOLVER));
        assertThrows(IllegalArgumentException.class, () -> AfsDbRecordDataDom.from(new byte[1], RESOLVER));
        assertThrows(IllegalArgumentException.class, () -> AfsDbRecordDataDom.from(new byte[2], RESOLVER));
        assertThrows(IllegalArgumentException.class, () -> AfsDbRecordDataDom.from(new byte[3], RESOLVER));
    }

    @Test
    void fromParsesSubtypeAndHostname() {
        byte[] rdata = new byte[] {0, 1, 5, 'a', 'f', 's', 'd', 'b', 7, 'e', 'x', 'a', 'm', 'p', 'l', 'e', 0};

        RDataDom dom = AfsDbRecordDataDom.from(rdata, RESOLVER);
        AfsDbRecordDataDom afsdb = assertInstanceOf(AfsDbRecordDataDom.class, dom);

        assertEquals(1, afsdb.subtype());
        assertEquals(List.of("afsdb", "example"), afsdb.hostname().labelStrings());
    }

    @Test
    void fromUsesResolverForCompressedName() {
        byte[] rdata = new byte[] {0, 2, (byte) 0xC0, 0x10};

        RDataDom dom = AfsDbRecordDataDom.from(rdata, RESOLVER);
        AfsDbRecordDataDom afsdb = assertInstanceOf(AfsDbRecordDataDom.class, dom);

        assertEquals(2, afsdb.subtype());
        assertEquals(List.of("afsdb", "example"), afsdb.hostname().labelStrings());
    }

    @Test
    void toRejectsNullHostname() {
        AfsDbRecordDataDom dom = AfsDbRecordDataDom.builder().subtype(1).build();
        assertThrows(IllegalArgumentException.class, dom::to);
    }

    @Test
    void toRejectsInvalidSubtype() {
        DnsNameDom name = DnsNameDom.labels(List.of("afsdb", "example"));
        AfsDbRecordDataDom negative = AfsDbRecordDataDom.builder().subtype(-1).hostname(name).build();
        AfsDbRecordDataDom tooLarge = AfsDbRecordDataDom.builder().subtype(0x1_0000).hostname(name).build();

        assertThrows(IllegalArgumentException.class, negative::to);
        assertThrows(IllegalArgumentException.class, tooLarge::to);
    }

    @Test
    void toSerializesSubtypeAndHostname() {
        DnsNameDom name = DnsNameDom.labels(List.of("afsdb", "example"));
        AfsDbRecordDataDom dom = AfsDbRecordDataDom.builder().subtype(1).hostname(name).build();

        byte[] encoded = dom.to();

        assertArrayEquals(
                TestBytes.concat(
                        new byte[] {0, 1},
                        new byte[] {5, 'a', 'f', 's', 'd', 'b', 7, 'e', 'x', 'a', 'm', 'p', 'l', 'e', 0}
                ),
                encoded
        );
    }

    @Test
    void roundTripPreservesFields() {
        DnsNameDom name = DnsNameDom.labels(List.of("afsdb", "example"));
        AfsDbRecordDataDom original = AfsDbRecordDataDom.builder().subtype(2).hostname(name).build();

        RDataDom decoded = AfsDbRecordDataDom.from(original.to(), RESOLVER);
        AfsDbRecordDataDom parsed = assertInstanceOf(AfsDbRecordDataDom.class, decoded);

        assertEquals(2, parsed.subtype());
        assertEquals(List.of("afsdb", "example"), parsed.hostname().labelStrings());
    }
}
