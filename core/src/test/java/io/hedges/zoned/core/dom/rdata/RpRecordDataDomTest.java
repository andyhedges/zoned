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

class RpRecordDataDomTest {
    private static final NameResolver RESOLVER =
            offset -> DnsNameDom.labels(List.of("txt", "example"));

    @Test
    void fromRejectsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> RpRecordDataDom.from(null, RESOLVER));
        assertThrows(IllegalArgumentException.class, () -> RpRecordDataDom.from(new byte[0], RESOLVER));
    }

    @Test
    void fromRejectsMissingTextDomain() {
        byte[] rdata = new byte[] {3, 'm', 'b', 'x', 0};
        assertThrows(IllegalArgumentException.class, () -> RpRecordDataDom.from(rdata, RESOLVER));
    }

    @Test
    void fromRejectsExtraBytesAfterTextDomain() {
        byte[] rdata = new byte[] {3, 'm', 'b', 'x', 0, 0, 1};
        assertThrows(IllegalArgumentException.class, () -> RpRecordDataDom.from(rdata, RESOLVER));
    }

    @Test
    void fromParsesFields() {
        byte[] rdata = new byte[] {3, 'm', 'b', 'x', 7, 'e', 'x', 'a', 'm', 'p', 'l', 'e', 4, 't', 'e', 's', 't', 0,
                3, 't', 'x', 't', 7, 'e', 'x', 'a', 'm', 'p', 'l', 'e', 4, 't', 'e', 's', 't', 0};

        RDataDom dom = RpRecordDataDom.from(rdata, RESOLVER);
        RpRecordDataDom rp = assertInstanceOf(RpRecordDataDom.class, dom);

        assertEquals(List.of("mbx", "example", "test"), rp.mailbox().labelStrings());
        assertEquals(List.of("txt", "example", "test"), rp.textDomain().labelStrings());
    }

    @Test
    void fromUsesResolverForCompressedNames() {
        byte[] rdata = new byte[] {3, 'm', 'b', 'x', 0, (byte) 0xC0, 0x10};

        RDataDom dom = RpRecordDataDom.from(rdata, RESOLVER);
        RpRecordDataDom rp = assertInstanceOf(RpRecordDataDom.class, dom);

        assertEquals(List.of("mbx"), rp.mailbox().labelStrings());
        assertEquals(List.of("txt", "example"), rp.textDomain().labelStrings());
    }

    @Test
    void toRejectsInvalidFields() {
        RpRecordDataDom missingMailbox = RpRecordDataDom.builder()
                .textDomain(DnsNameDom.labels(List.of("txt", "example")))
                .build();
        RpRecordDataDom missingText = RpRecordDataDom.builder()
                .mailbox(DnsNameDom.labels(List.of("mbx", "example")))
                .build();

        assertThrows(IllegalArgumentException.class, missingMailbox::to);
        assertThrows(IllegalArgumentException.class, missingText::to);
    }

    @Test
    void toSerializesRdata() {
        DnsNameDom mailbox = DnsNameDom.labels(List.of("mbx", "example", "test"));
        DnsNameDom text = DnsNameDom.labels(List.of("txt", "example", "test"));
        RpRecordDataDom dom = RpRecordDataDom.builder().mailbox(mailbox).textDomain(text).build();

        byte[] mailboxBytes = RDataUtils.toByteArray(mailbox);
        byte[] textBytes = RDataUtils.toByteArray(text);
        byte[] expected = new byte[mailboxBytes.length + textBytes.length];
        System.arraycopy(mailboxBytes, 0, expected, 0, mailboxBytes.length);
        System.arraycopy(textBytes, 0, expected, mailboxBytes.length, textBytes.length);

        assertArrayEquals(expected, dom.to());
    }

    @Test
    void roundTripPreservesFields() {
        DnsNameDom mailbox = DnsNameDom.labels(List.of("mbx", "example", "test"));
        DnsNameDom text = DnsNameDom.labels(List.of("txt", "example", "test"));
        RpRecordDataDom original = RpRecordDataDom.builder().mailbox(mailbox).textDomain(text).build();

        RDataDom decoded = RpRecordDataDom.from(original.to(), RESOLVER);
        RpRecordDataDom parsed = assertInstanceOf(RpRecordDataDom.class, decoded);

        assertEquals(List.of("mbx", "example", "test"), parsed.mailbox().labelStrings());
        assertEquals(List.of("txt", "example", "test"), parsed.textDomain().labelStrings());
    }
}
