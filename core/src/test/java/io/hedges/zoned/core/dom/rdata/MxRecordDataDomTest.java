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

class MxRecordDataDomTest {

    @Test
    void fromRejectsShortInput() {
        assertThrows(IllegalArgumentException.class, () -> MxRecordDataDom.from(null, offset -> null));
        assertThrows(IllegalArgumentException.class, () -> MxRecordDataDom.from(new byte[0], offset -> null));
        assertThrows(IllegalArgumentException.class, () -> MxRecordDataDom.from(new byte[1], offset -> null));
        assertThrows(IllegalArgumentException.class, () -> MxRecordDataDom.from(new byte[2], offset -> null));
    }

    @Test
    void fromParsesPreferenceAndExchange() {
        byte[] rdata = new byte[] {0, 10, 3, 'm', 'x', '1', 7, 'e', 'x', 'a', 'm', 'p', 'l', 'e', 0};

        RDataDom dom = MxRecordDataDom.from(rdata, offset -> null);
        MxRecordDataDom mx = assertInstanceOf(MxRecordDataDom.class, dom);

        assertEquals(10, mx.preference());
        assertEquals(List.of("mx1", "example"), mx.exchange().labels());
    }

    @Test
    void fromUsesResolverForCompressedName() {
        byte[] rdata = new byte[] {0, 5, (byte) 0xC0, 0x10};
        NameResolver resolver = offset -> DnsNameDom.builder().labels(List.of("mail", "example")).build();

        RDataDom dom = MxRecordDataDom.from(rdata, resolver);
        MxRecordDataDom mx = assertInstanceOf(MxRecordDataDom.class, dom);

        assertEquals(5, mx.preference());
        assertEquals(List.of("mail", "example"), mx.exchange().labels());
    }

    @Test
    void toRejectsNullExchange() {
        MxRecordDataDom dom = MxRecordDataDom.builder().preference(10).build();
        assertThrows(IllegalArgumentException.class, dom::to);
    }

    @Test
    void toRejectsInvalidPreference() {
        DnsNameDom name = DnsNameDom.builder().labels(List.of("mx", "example")).build();
        MxRecordDataDom negative = MxRecordDataDom.builder().preference(-1).exchange(name).build();
        MxRecordDataDom tooLarge = MxRecordDataDom.builder().preference(0x1_0000).exchange(name).build();

        assertThrows(IllegalArgumentException.class, negative::to);
        assertThrows(IllegalArgumentException.class, tooLarge::to);
    }

    @Test
    void toSerializesPreferenceAndExchange() {
        DnsNameDom name = DnsNameDom.builder().labels(List.of("mx1", "example")).build();
        MxRecordDataDom dom = MxRecordDataDom.builder().preference(10).exchange(name).build();

        byte[] encoded = dom.to();

        assertArrayEquals(
                TestBytes.concat(
                        new byte[] {0, 10},
                        new byte[] {3, 'm', 'x', '1', 7, 'e', 'x', 'a', 'm', 'p', 'l', 'e', 0}
                ),
                encoded
        );
    }

    @Test
    void roundTripPreservesFields() {
        DnsNameDom name = DnsNameDom.builder().labels(List.of("mail", "example")).build();
        MxRecordDataDom original = MxRecordDataDom.builder().preference(25).exchange(name).build();

        RDataDom decoded = MxRecordDataDom.from(original.to(), offset -> null);
        MxRecordDataDom parsed = assertInstanceOf(MxRecordDataDom.class, decoded);

        assertEquals(25, parsed.preference());
        assertEquals(List.of("mail", "example"), parsed.exchange().labels());
    }
}
