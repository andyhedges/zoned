// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.DnsNameDom;
import io.hedges.zoned.core.dom.RDataDom;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NaptrRecordDataDomTest {

    @Test
    void fromRejectsInvalidLength() {
        assertThrows(IllegalArgumentException.class, () -> NaptrRecordDataDom.from(null));
        assertThrows(IllegalArgumentException.class, () -> NaptrRecordDataDom.from(new byte[6]));
    }

    @Test
    void fromRejectsTruncatedStrings() {
        byte[] rdata = new byte[] {0, 1, 0, 2, 5, 'f'};
        assertThrows(IllegalArgumentException.class, () -> NaptrRecordDataDom.from(rdata));
    }

    @Test
    void fromRejectsMissingReplacement() {
        byte[] rdata = new byte[] {0, 1, 0, 2, 0, 0, 0};
        assertThrows(IllegalArgumentException.class, () -> NaptrRecordDataDom.from(rdata));
    }

    @Test
    void fromParsesFields() {
        DnsNameDom replacement = DnsNameDom.builder().labels(List.of("example", "test")).build();
        byte[] nameBytes = RDataUtils.toByteArray(replacement);
        byte[] flags = new byte[] {1, 'U'};
        byte[] services = new byte[] {7, 'E', '2', 'U', '+', 's', 'i', 'p'};
        byte[] regexp = new byte[] {1, '!'};
        byte[] rdata = new byte[4 + flags.length + services.length + regexp.length + nameBytes.length];
        int idx = 0;
        rdata[idx++] = 0;
        rdata[idx++] = 10;
        rdata[idx++] = 0;
        rdata[idx++] = 20;
        System.arraycopy(flags, 0, rdata, idx, flags.length);
        idx += flags.length;
        System.arraycopy(services, 0, rdata, idx, services.length);
        idx += services.length;
        System.arraycopy(regexp, 0, rdata, idx, regexp.length);
        idx += regexp.length;
        System.arraycopy(nameBytes, 0, rdata, idx, nameBytes.length);

        RDataDom dom = NaptrRecordDataDom.from(rdata);
        NaptrRecordDataDom naptr = assertInstanceOf(NaptrRecordDataDom.class, dom);

        assertEquals(10, naptr.order());
        assertEquals(20, naptr.preference());
        assertEquals("U", naptr.flags());
        assertEquals("E2U+sip", naptr.services());
        assertEquals("!", naptr.regexp());
        assertEquals(List.of("example", "test"), naptr.replacement().labels());
    }

    @Test
    void toRejectsInvalidFields() {
        NaptrRecordDataDom missingStrings = NaptrRecordDataDom.builder()
                .order(1)
                .preference(2)
                .replacement(DnsNameDom.builder().labels(List.of("example", "test")).build())
                .build();
        NaptrRecordDataDom negativeOrder = NaptrRecordDataDom.builder()
                .order(-1)
                .preference(2)
                .flags("")
                .services("")
                .regexp("")
                .replacement(DnsNameDom.builder().labels(List.of("example", "test")).build())
                .build();
        NaptrRecordDataDom tooLargeOrder = NaptrRecordDataDom.builder()
                .order(0x1_0000)
                .preference(2)
                .flags("")
                .services("")
                .regexp("")
                .replacement(DnsNameDom.builder().labels(List.of("example", "test")).build())
                .build();
        NaptrRecordDataDom negativePreference = NaptrRecordDataDom.builder()
                .order(1)
                .preference(-1)
                .flags("")
                .services("")
                .regexp("")
                .replacement(DnsNameDom.builder().labels(List.of("example", "test")).build())
                .build();
        NaptrRecordDataDom tooLargePreference = NaptrRecordDataDom.builder()
                .order(1)
                .preference(0x1_0000)
                .flags("")
                .services("")
                .regexp("")
                .replacement(DnsNameDom.builder().labels(List.of("example", "test")).build())
                .build();
        NaptrRecordDataDom badFlags = NaptrRecordDataDom.builder()
                .order(1)
                .preference(2)
                .flags("a".repeat(256))
                .services("")
                .regexp("")
                .replacement(DnsNameDom.builder().labels(List.of("example", "test")).build())
                .build();
        NaptrRecordDataDom badServices = NaptrRecordDataDom.builder()
                .order(1)
                .preference(2)
                .flags("")
                .services("b".repeat(256))
                .regexp("")
                .replacement(DnsNameDom.builder().labels(List.of("example", "test")).build())
                .build();
        NaptrRecordDataDom badRegexp = NaptrRecordDataDom.builder()
                .order(1)
                .preference(2)
                .flags("")
                .services("")
                .regexp("c".repeat(256))
                .replacement(DnsNameDom.builder().labels(List.of("example", "test")).build())
                .build();
        NaptrRecordDataDom missingReplacement = NaptrRecordDataDom.builder()
                .order(1)
                .preference(2)
                .flags("")
                .services("")
                .regexp("")
                .build();

        assertThrows(IllegalArgumentException.class, missingStrings::to);
        assertThrows(IllegalArgumentException.class, negativeOrder::to);
        assertThrows(IllegalArgumentException.class, tooLargeOrder::to);
        assertThrows(IllegalArgumentException.class, negativePreference::to);
        assertThrows(IllegalArgumentException.class, tooLargePreference::to);
        assertThrows(IllegalArgumentException.class, badFlags::to);
        assertThrows(IllegalArgumentException.class, badServices::to);
        assertThrows(IllegalArgumentException.class, badRegexp::to);
        assertThrows(IllegalArgumentException.class, missingReplacement::to);
    }

    @Test
    void toSerializesRdata() {
        DnsNameDom replacement = DnsNameDom.builder().labels(List.of("example", "test")).build();
        NaptrRecordDataDom dom = NaptrRecordDataDom.builder()
                .order(10)
                .preference(20)
                .flags("U")
                .services("E2U+sip")
                .regexp("!")
                .replacement(replacement)
                .build();

        byte[] nameBytes = RDataUtils.toByteArray(replacement);
        byte[] flags = "U".getBytes(StandardCharsets.US_ASCII);
        byte[] services = "E2U+sip".getBytes(StandardCharsets.US_ASCII);
        byte[] regexp = "!".getBytes(StandardCharsets.US_ASCII);
        byte[] expected = new byte[4 + 1 + flags.length + 1 + services.length + 1 + regexp.length + nameBytes.length];
        int idx = 0;
        expected[idx++] = 0;
        expected[idx++] = 10;
        expected[idx++] = 0;
        expected[idx++] = 20;
        expected[idx++] = (byte) flags.length;
        System.arraycopy(flags, 0, expected, idx, flags.length);
        idx += flags.length;
        expected[idx++] = (byte) services.length;
        System.arraycopy(services, 0, expected, idx, services.length);
        idx += services.length;
        expected[idx++] = (byte) regexp.length;
        System.arraycopy(regexp, 0, expected, idx, regexp.length);
        idx += regexp.length;
        System.arraycopy(nameBytes, 0, expected, idx, nameBytes.length);

        assertArrayEquals(expected, dom.to());
    }

    @Test
    void roundTripPreservesFields() {
        DnsNameDom replacement = DnsNameDom.builder().labels(List.of("example", "test")).build();
        NaptrRecordDataDom original = NaptrRecordDataDom.builder()
                .order(100)
                .preference(200)
                .flags("U")
                .services("E2U+sip")
                .regexp("!")
                .replacement(replacement)
                .build();

        RDataDom decoded = NaptrRecordDataDom.from(original.to());
        NaptrRecordDataDom parsed = assertInstanceOf(NaptrRecordDataDom.class, decoded);

        assertEquals(100, parsed.order());
        assertEquals(200, parsed.preference());
        assertEquals("U", parsed.flags());
        assertEquals("E2U+sip", parsed.services());
        assertEquals("!", parsed.regexp());
        assertEquals(List.of("example", "test"), parsed.replacement().labels());
    }
}
