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

class CnameRecordDataDomTest {
    private static final NameResolver RESOLVER =
            offset -> DnsNameDom.builder().labels(List.of("alias", "example")).build();

    @Test
    void fromRejectsInvalidName() {
        assertThrows(IllegalArgumentException.class, () -> CnameRecordDataDom.from(null, RESOLVER));
        assertThrows(IllegalArgumentException.class, () -> CnameRecordDataDom.from(new byte[0], RESOLVER));
    }

    @Test
    void fromParsesName() {
        byte[] rdata = new byte[] {3, 'w', 'w', 'w', 7, 'e', 'x', 'a', 'm', 'p', 'l', 'e', 4, 't', 'e', 's', 't', 0};

        RDataDom dom = CnameRecordDataDom.from(rdata, RESOLVER);
        CnameRecordDataDom cname = assertInstanceOf(CnameRecordDataDom.class, dom);

        assertEquals(List.of("www", "example", "test"), cname.cname().labels());
    }

    @Test
    void fromUsesResolverForCompressedNames() {
        byte[] rdata = new byte[] {(byte) 0xC0, 0x10};

        RDataDom dom = CnameRecordDataDom.from(rdata, RESOLVER);
        CnameRecordDataDom cname = assertInstanceOf(CnameRecordDataDom.class, dom);

        assertEquals(List.of("alias", "example"), cname.cname().labels());
    }

    @Test
    void toRejectsNullCname() {
        CnameRecordDataDom dom = CnameRecordDataDom.builder().build();
        assertThrows(IllegalArgumentException.class, dom::to);
    }

    @Test
    void toSerializesName() {
        DnsNameDom name = DnsNameDom.builder().labels(List.of("www", "example", "test")).build();
        CnameRecordDataDom dom = CnameRecordDataDom.builder().cname(name).build();

        byte[] encoded = dom.to();

        assertArrayEquals(
                new byte[] {3, 'w', 'w', 'w', 7, 'e', 'x', 'a', 'm', 'p', 'l', 'e', 4, 't', 'e', 's', 't', 0},
                encoded
        );
    }

    @Test
    void roundTripPreservesName() {
        DnsNameDom name = DnsNameDom.builder().labels(List.of("service", "example", "test")).build();
        CnameRecordDataDom original = CnameRecordDataDom.builder().cname(name).build();

        RDataDom decoded = CnameRecordDataDom.from(original.to(), RESOLVER);
        CnameRecordDataDom parsed = assertInstanceOf(CnameRecordDataDom.class, decoded);

        assertEquals(List.of("service", "example", "test"), parsed.cname().labels());
    }
}
