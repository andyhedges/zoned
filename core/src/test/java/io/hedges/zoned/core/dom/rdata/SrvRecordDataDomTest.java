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

class SrvRecordDataDomTest {
    private static final NameResolver RESOLVER =
            offset -> DnsNameDom.builder().labels(List.of("srv", "example")).build();

    @Test
    void fromRejectsInvalidLength() {
        assertThrows(IllegalArgumentException.class, () -> SrvRecordDataDom.from(null, RESOLVER));
        assertThrows(IllegalArgumentException.class, () -> SrvRecordDataDom.from(new byte[0], RESOLVER));
        assertThrows(IllegalArgumentException.class, () -> SrvRecordDataDom.from(new byte[6], RESOLVER));
    }

    @Test
    void fromRejectsExtraBytes() {
        byte[] rdata = new byte[] {
                0, 1, 0, 2, 0, 3,
                3, 's', 'r', 'v', 7, 'e', 'x', 'a', 'm', 'p', 'l', 'e', 4, 't', 'e', 's', 't', 0,
                9
        };
        assertThrows(IllegalArgumentException.class, () -> SrvRecordDataDom.from(rdata, RESOLVER));
    }

    @Test
    void fromParsesFields() {
        byte[] rdata = new byte[] {
                0, 10, 0, 20, 0, 30,
                3, 's', 'r', 'v', 7, 'e', 'x', 'a', 'm', 'p', 'l', 'e', 4, 't', 'e', 's', 't', 0
        };

        RDataDom dom = SrvRecordDataDom.from(rdata, RESOLVER);
        SrvRecordDataDom srv = assertInstanceOf(SrvRecordDataDom.class, dom);

        assertEquals(10, srv.priority());
        assertEquals(20, srv.weight());
        assertEquals(30, srv.port());
        assertEquals(List.of("srv", "example", "test"), srv.target().labels());
    }

    @Test
    void fromUsesResolverForCompressedNames() {
        byte[] rdata = new byte[] {0, 1, 0, 2, 0, 3, (byte) 0xC0, 0x10};

        RDataDom dom = SrvRecordDataDom.from(rdata, RESOLVER);
        SrvRecordDataDom srv = assertInstanceOf(SrvRecordDataDom.class, dom);

        assertEquals(1, srv.priority());
        assertEquals(2, srv.weight());
        assertEquals(3, srv.port());
        assertEquals(List.of("srv", "example"), srv.target().labels());
    }

    @Test
    void toRejectsInvalidFields() {
        SrvRecordDataDom missingTarget = SrvRecordDataDom.builder()
                .priority(1)
                .weight(2)
                .port(3)
                .build();
        SrvRecordDataDom negativePriority = SrvRecordDataDom.builder()
                .priority(-1)
                .weight(2)
                .port(3)
                .target(DnsNameDom.builder().labels(List.of("srv", "example")).build())
                .build();
        SrvRecordDataDom tooLargePriority = SrvRecordDataDom.builder()
                .priority(0x1_0000)
                .weight(2)
                .port(3)
                .target(DnsNameDom.builder().labels(List.of("srv", "example")).build())
                .build();
        SrvRecordDataDom negativeWeight = SrvRecordDataDom.builder()
                .priority(1)
                .weight(-1)
                .port(3)
                .target(DnsNameDom.builder().labels(List.of("srv", "example")).build())
                .build();
        SrvRecordDataDom tooLargeWeight = SrvRecordDataDom.builder()
                .priority(1)
                .weight(0x1_0000)
                .port(3)
                .target(DnsNameDom.builder().labels(List.of("srv", "example")).build())
                .build();
        SrvRecordDataDom negativePort = SrvRecordDataDom.builder()
                .priority(1)
                .weight(2)
                .port(-1)
                .target(DnsNameDom.builder().labels(List.of("srv", "example")).build())
                .build();
        SrvRecordDataDom tooLargePort = SrvRecordDataDom.builder()
                .priority(1)
                .weight(2)
                .port(0x1_0000)
                .target(DnsNameDom.builder().labels(List.of("srv", "example")).build())
                .build();

        assertThrows(IllegalArgumentException.class, missingTarget::to);
        assertThrows(IllegalArgumentException.class, negativePriority::to);
        assertThrows(IllegalArgumentException.class, tooLargePriority::to);
        assertThrows(IllegalArgumentException.class, negativeWeight::to);
        assertThrows(IllegalArgumentException.class, tooLargeWeight::to);
        assertThrows(IllegalArgumentException.class, negativePort::to);
        assertThrows(IllegalArgumentException.class, tooLargePort::to);
    }

    @Test
    void toSerializesRdata() {
        DnsNameDom name = DnsNameDom.builder().labels(List.of("srv", "example", "test")).build();
        SrvRecordDataDom dom = SrvRecordDataDom.builder()
                .priority(10)
                .weight(20)
                .port(30)
                .target(name)
                .build();

        byte[] expected = new byte[] {
                0, 10, 0, 20, 0, 30,
                3, 's', 'r', 'v', 7, 'e', 'x', 'a', 'm', 'p', 'l', 'e', 4, 't', 'e', 's', 't', 0
        };
        assertArrayEquals(expected, dom.to());
    }

    @Test
    void roundTripPreservesFields() {
        DnsNameDom name = DnsNameDom.builder().labels(List.of("srv", "example", "test")).build();
        SrvRecordDataDom original = SrvRecordDataDom.builder()
                .priority(1)
                .weight(2)
                .port(3)
                .target(name)
                .build();

        RDataDom decoded = SrvRecordDataDom.from(original.to(), RESOLVER);
        SrvRecordDataDom parsed = assertInstanceOf(SrvRecordDataDom.class, decoded);

        assertEquals(1, parsed.priority());
        assertEquals(2, parsed.weight());
        assertEquals(3, parsed.port());
        assertEquals(List.of("srv", "example", "test"), parsed.target().labels());
    }
}
