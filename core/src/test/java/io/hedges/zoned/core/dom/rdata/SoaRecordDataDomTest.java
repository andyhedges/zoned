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

class SoaRecordDataDomTest {
    private static final NameResolver RESOLVER =
            offset -> DnsNameDom.builder().labels(List.of("ns", "example")).build();

    @Test
    void fromRejectsInvalidData() {
        assertThrows(IllegalArgumentException.class, () -> SoaRecordDataDom.from(null, RESOLVER));
        assertThrows(IllegalArgumentException.class, () -> SoaRecordDataDom.from(new byte[0], RESOLVER));
        assertThrows(IllegalArgumentException.class, () -> SoaRecordDataDom.from(new byte[] {2, 'n', 's', 0}, RESOLVER));
        assertThrows(IllegalArgumentException.class, () -> SoaRecordDataDom.from(new byte[] {2, 'n', 's', 0, 0}, RESOLVER));
    }

    @Test
    void fromParsesFields() {
        byte[] rdata = TestBytes.concat(
                new byte[] {2, 'n', 's', 7, 'e', 'x', 'a', 'm', 'p', 'l', 'e', 0},
                new byte[] {10, 'h', 'o', 's', 't', 'm', 'a', 's', 't', 'e', 'r', 7, 'e', 'x', 'a', 'm', 'p', 'l', 'e', 0},
                new byte[] {0, 0, 0, 1},
                new byte[] {0, 0, 0, 2},
                new byte[] {0, 0, 0, 3},
                new byte[] {0, 0, 0, 4},
                new byte[] {0, 0, 0, 5}
        );

        RDataDom dom = SoaRecordDataDom.from(rdata, RESOLVER);
        SoaRecordDataDom soa = assertInstanceOf(SoaRecordDataDom.class, dom);

        assertEquals(List.of("ns", "example"), soa.mname().labels());
        assertEquals(List.of("hostmaster", "example"), soa.rname().labels());
        assertEquals(1L, soa.serial());
        assertEquals(2L, soa.refreshSeconds());
        assertEquals(3L, soa.retrySeconds());
        assertEquals(4L, soa.expireSeconds());
        assertEquals(5L, soa.minimumTtlSeconds());
    }

    @Test
    void fromUsesResolverForCompressedMname() {
        byte[] rdata = TestBytes.concat(
                new byte[] {(byte) 0xC0, 0x10},
                new byte[] {10, 'h', 'o', 's', 't', 'm', 'a', 's', 't', 'e', 'r', 7, 'e', 'x', 'a', 'm', 'p', 'l', 'e', 0},
                new byte[] {0, 0, 0, 1},
                new byte[] {0, 0, 0, 2},
                new byte[] {0, 0, 0, 3},
                new byte[] {0, 0, 0, 4},
                new byte[] {0, 0, 0, 5}
        );

        RDataDom dom = SoaRecordDataDom.from(rdata, RESOLVER);
        SoaRecordDataDom soa = assertInstanceOf(SoaRecordDataDom.class, dom);

        assertEquals(List.of("ns", "example"), soa.mname().labels());
        assertEquals(List.of("hostmaster", "example"), soa.rname().labels());
    }

    @Test
    void toRejectsInvalidFields() {
        DnsNameDom name = DnsNameDom.builder().labels(List.of("ns", "example")).build();
        SoaRecordDataDom missingNames = SoaRecordDataDom.builder().build();
        SoaRecordDataDom missingRname = SoaRecordDataDom.builder().mname(name).build();
        SoaRecordDataDom negative = SoaRecordDataDom.builder()
                .mname(name)
                .rname(name)
                .serial(-1)
                .build();
        SoaRecordDataDom tooLarge = SoaRecordDataDom.builder()
                .mname(name)
                .rname(name)
                .serial(0x1_0000_0000L)
                .build();

        assertThrows(IllegalArgumentException.class, missingNames::to);
        assertThrows(IllegalArgumentException.class, missingRname::to);
        assertThrows(IllegalArgumentException.class, negative::to);
        assertThrows(IllegalArgumentException.class, tooLarge::to);
    }

    @Test
    void toSerializesFields() {
        DnsNameDom mname = DnsNameDom.builder().labels(List.of("ns", "example")).build();
        DnsNameDom rname = DnsNameDom.builder().labels(List.of("hostmaster", "example")).build();
        SoaRecordDataDom dom = SoaRecordDataDom.builder()
                .mname(mname)
                .rname(rname)
                .serial(10L)
                .refreshSeconds(20L)
                .retrySeconds(30L)
                .expireSeconds(40L)
                .minimumTtlSeconds(50L)
                .build();

        byte[] encoded = dom.to();

        assertArrayEquals(
                TestBytes.concat(
                        new byte[] {2, 'n', 's', 7, 'e', 'x', 'a', 'm', 'p', 'l', 'e', 0},
                        new byte[] {10, 'h', 'o', 's', 't', 'm', 'a', 's', 't', 'e', 'r', 7, 'e', 'x', 'a', 'm', 'p', 'l', 'e', 0},
                        new byte[] {0, 0, 0, 10},
                        new byte[] {0, 0, 0, 20},
                        new byte[] {0, 0, 0, 30},
                        new byte[] {0, 0, 0, 40},
                        new byte[] {0, 0, 0, 50}
                ),
                encoded
        );
    }

    @Test
    void roundTripPreservesFields() {
        DnsNameDom mname = DnsNameDom.builder().labels(List.of("ns", "example")).build();
        DnsNameDom rname = DnsNameDom.builder().labels(List.of("hostmaster", "example")).build();
        SoaRecordDataDom original = SoaRecordDataDom.builder()
                .mname(mname)
                .rname(rname)
                .serial(12345L)
                .refreshSeconds(7200L)
                .retrySeconds(3600L)
                .expireSeconds(1209600L)
                .minimumTtlSeconds(300L)
                .build();

        RDataDom decoded = SoaRecordDataDom.from(original.to(), RESOLVER);
        SoaRecordDataDom parsed = assertInstanceOf(SoaRecordDataDom.class, decoded);

        assertEquals(List.of("ns", "example"), parsed.mname().labels());
        assertEquals(List.of("hostmaster", "example"), parsed.rname().labels());
        assertEquals(12345L, parsed.serial());
        assertEquals(7200L, parsed.refreshSeconds());
        assertEquals(3600L, parsed.retrySeconds());
        assertEquals(1209600L, parsed.expireSeconds());
        assertEquals(300L, parsed.minimumTtlSeconds());
    }
}
