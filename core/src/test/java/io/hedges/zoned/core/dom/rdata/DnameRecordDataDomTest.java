// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.NameResolver;
import io.hedges.zoned.core.dom.DnsNameDom;
import io.hedges.zoned.core.dom.RDataDom;
import io.hedges.zoned.core.dom.DnsNameDomPolicy;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DnameRecordDataDomTest {
    private static final NameResolver RESOLVER =
            offset -> DnsNameDom.labels(DnsNameDomPolicy.Builtin.PROTOCOL, List.of("alias", "example"));

    @Test
    void fromRejectsInvalidName() {
        assertThrows(IllegalArgumentException.class, () -> DnameRecordDataDom.from(null, RESOLVER));
        assertThrows(IllegalArgumentException.class, () -> DnameRecordDataDom.from(new byte[0], RESOLVER));
    }

    @Test
    void fromParsesName() {
        byte[] rdata = new byte[] {3, 't', 'g', 't', 7, 'e', 'x', 'a', 'm', 'p', 'l', 'e', 4, 't', 'e', 's', 't', 0};

        RDataDom dom = DnameRecordDataDom.from(rdata, RESOLVER);
        DnameRecordDataDom dname = assertInstanceOf(DnameRecordDataDom.class, dom);

        assertEquals(List.of("tgt", "example", "test"), dname.dname().labelStrings());
    }

    @Test
    void fromUsesResolverForCompressedNames() {
        byte[] rdata = new byte[] {(byte) 0xC0, 0x10};

        RDataDom dom = DnameRecordDataDom.from(rdata, RESOLVER);
        DnameRecordDataDom dname = assertInstanceOf(DnameRecordDataDom.class, dom);

        assertEquals(List.of("alias", "example"), dname.dname().labelStrings());
    }

    @Test
    void toRejectsNullDname() {
        DnameRecordDataDom dom = DnameRecordDataDom.builder().build();
        assertThrows(IllegalArgumentException.class, dom::to);
    }

    @Test
    void toSerializesName() {
        DnsNameDom name = DnsNameDom.labels(DnsNameDomPolicy.Builtin.PROTOCOL, List.of("tgt", "example", "test"));
        DnameRecordDataDom dom = DnameRecordDataDom.builder().dname(name).build();

        byte[] encoded = dom.to();

        assertArrayEquals(
                new byte[] {3, 't', 'g', 't', 7, 'e', 'x', 'a', 'm', 'p', 'l', 'e', 4, 't', 'e', 's', 't', 0},
                encoded
        );
    }

    @Test
    void roundTripPreservesName() {
        DnsNameDom name = DnsNameDom.labels(DnsNameDomPolicy.Builtin.PROTOCOL, List.of("service", "example", "test"));
        DnameRecordDataDom original = DnameRecordDataDom.builder().dname(name).build();

        RDataDom decoded = DnameRecordDataDom.from(original.to(), RESOLVER);
        DnameRecordDataDom parsed = assertInstanceOf(DnameRecordDataDom.class, decoded);

        assertEquals(List.of("service", "example", "test"), parsed.dname().labelStrings());
    }
}