// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.NameResolver;
import io.hedges.zoned.core.dom.DnsNameDom;
import org.junit.jupiter.api.Test;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RDataUtilsTest {

    @Test
    void toInet4AddressValidatesLength() {
        assertThrows(IllegalArgumentException.class, () -> RDataUtils.toInet4Address(null));
        assertThrows(IllegalArgumentException.class, () -> RDataUtils.toInet4Address(new byte[3]));
        assertThrows(IllegalArgumentException.class, () -> RDataUtils.toInet4Address(new byte[5]));
    }

    @Test
    void toInet4AddressParsesValidBytes() {
        byte[] bytes = new byte[] {1, 2, 3, 4};
        Inet4Address addr = RDataUtils.toInet4Address(bytes);
        assertEquals("1.2.3.4", addr.getHostAddress());
    }

    @Test
    void toInet6AddressValidatesLength() {
        assertThrows(IllegalArgumentException.class, () -> RDataUtils.toInet6Address(null));
        assertThrows(IllegalArgumentException.class, () -> RDataUtils.toInet6Address(new byte[15]));
        assertThrows(IllegalArgumentException.class, () -> RDataUtils.toInet6Address(new byte[17]));
    }

    @Test
    void toInet6AddressParsesValidBytes() {
        byte[] bytes = new byte[] {
                0x20, 0x01, 0x0d, (byte) 0xb8,
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 1
        };
        Inet6Address addr = RDataUtils.toInet6Address(bytes);
        assertArrayEquals(bytes, addr.getAddress());
    }

    @Test
    void toByteArrayFromInet4AddressCopiesBytes() throws Exception {
        InetAddress addr = InetAddress.getByAddress(new byte[] {(byte) 192, 0, 2, 1});
        Inet4Address inet4 = assertInstanceOf(Inet4Address.class, addr);

        byte[] bytes = RDataUtils.toByteArray(inet4);

        assertArrayEquals(new byte[] {(byte) 192, 0, 2, 1}, bytes);
    }

    @Test
    void toByteArrayFromInet6AddressCopiesBytes() throws Exception {
        byte[] raw = new byte[] {
                0x20, 0x01, 0x0d, (byte) 0xb8,
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 1
        };
        InetAddress addr = InetAddress.getByAddress(raw);
        Inet6Address inet6 = assertInstanceOf(Inet6Address.class, addr);

        byte[] bytes = RDataUtils.toByteArray(inet6);

        assertArrayEquals(raw, bytes);
    }

    @Test
    void toByteArrayRejectsNullInetAddresses() {
        assertThrows(IllegalArgumentException.class, () -> RDataUtils.toByteArray((Inet4Address) null));
        assertThrows(IllegalArgumentException.class, () -> RDataUtils.toByteArray((Inet6Address) null));
    }

    @Test
    void toDnsNameDomRejectsEmptyInput() {
        assertThrows(IllegalArgumentException.class, () -> RDataUtils.toDnsNameDom(null));
        assertThrows(IllegalArgumentException.class, () -> RDataUtils.toDnsNameDom(new byte[0]));
    }

    @Test
    void toDnsNameDomRejectsOversizedInput() {
        assertThrows(IllegalArgumentException.class, () -> RDataUtils.toDnsNameDom(new byte[256]));
    }

    @Test
    void toDnsNameDomRejectsLongLabel() {
        byte[] rdata = new byte[65];
        rdata[0] = 64;
        assertThrows(IllegalArgumentException.class, () -> RDataUtils.toDnsNameDom(rdata));
    }

    @Test
    void toDnsNameDomRejectsTruncatedLabel() {
        byte[] rdata = new byte[] {3, 'a', 'b'};
        assertThrows(IllegalArgumentException.class, () -> RDataUtils.toDnsNameDom(rdata));
    }

    @Test
    void toDnsNameDomRejectsMissingLabels() {
        byte[] rdata = new byte[] {0};
        assertThrows(IllegalArgumentException.class, () -> RDataUtils.toDnsNameDom(rdata));
    }

    @Test
    void toDnsNameDomRejectsExtraBytesAfterName() {
        byte[] rdata = new byte[] {1, 'a', 0, 1};
        assertThrows(IllegalArgumentException.class, () -> RDataUtils.toDnsNameDom(rdata));
    }

    @Test
    void toDnsNameDomRejectsCompressedNameWithoutResolver() {
        byte[] rdata = new byte[] {(byte) 0xC0, 0x10};
        assertThrows(UnsupportedOperationException.class, () -> RDataUtils.toDnsNameDom(rdata, null));
    }

    @Test
    void toDnsNameDomRejectsTruncatedCompressionPointer() {
        byte[] rdata = new byte[] {(byte) 0xC0};
        assertThrows(IllegalArgumentException.class, () -> RDataUtils.toDnsNameDom(rdata, offset -> null));
    }

    @Test
    void toDnsNameDomRejectsNullResolvedName() {
        byte[] rdata = new byte[] {(byte) 0xC0, 0x10};
        assertThrows(IllegalArgumentException.class, () -> RDataUtils.toDnsNameDom(rdata, offset -> null));
    }

    @Test
    void toDnsNameDomRejectsResolvedNameWithNullLabels() {
        byte[] rdata = new byte[] {(byte) 0xC0, 0x10};
        NameResolver resolver = offset -> DnsNameDom.builder().build();
        assertThrows(IllegalArgumentException.class, () -> RDataUtils.toDnsNameDom(rdata, resolver));
    }

    @Test
    void toDnsNameDomResolvesCompressedName() {
        byte[] rdata = new byte[] {(byte) 0xC0, 0x10};
        NameResolver resolver = offset -> DnsNameDom.builder()
                .labels(List.of("alias", "example"))
                .build();

        DnsNameDom name = RDataUtils.toDnsNameDom(rdata, resolver);

        assertEquals(List.of("alias", "example"), name.labels());
    }

    @Test
    void toDnsNameDomParsesLabels() {
        byte[] rdata = new byte[] {3, 'w', 'w', 'w', 7, 'e', 'x', 'a', 'm', 'p', 'l', 'e', 4, 't', 'e', 's', 't', 0};
        DnsNameDom name = RDataUtils.toDnsNameDom(rdata);

        assertEquals(List.of("www", "example", "test"), name.labels());
    }

    @Test
    void toByteArrayFromDnsNameValidatesInputs() {
        assertThrows(IllegalArgumentException.class, () -> RDataUtils.toByteArray((DnsNameDom) null));
        assertThrows(IllegalArgumentException.class, () -> RDataUtils.toByteArray(DnsNameDom.builder().build()));
        assertThrows(IllegalArgumentException.class,
                     () -> RDataUtils.toByteArray(DnsNameDom.builder().labels(List.of()).build()));
    }

    @Test
    void toByteArrayFromDnsNameRejectsLongLabel() {
        String longLabel = "a".repeat(64);
        DnsNameDom name = DnsNameDom.builder().labels(List.of(longLabel)).build();

        assertThrows(IllegalArgumentException.class, () -> RDataUtils.toByteArray(name));
    }

    @Test
    void toByteArrayFromDnsNameRejectsOversizedName() {
        String label = "a".repeat(63);
        List<String> labels = List.of(label, label, label, label);
        DnsNameDom name = DnsNameDom.builder().labels(labels).build();

        assertThrows(IllegalArgumentException.class, () -> RDataUtils.toByteArray(name));
    }

    @Test
    void toByteArrayFromDnsNameSerializesLabels() {
        DnsNameDom name = DnsNameDom.builder().labels(List.of("www", "example", "test")).build();

        byte[] bytes = RDataUtils.toByteArray(name);

        assertArrayEquals(
                new byte[] {3, 'w', 'w', 'w', 7, 'e', 'x', 'a', 'm', 'p', 'l', 'e', 4, 't', 'e', 's', 't', 0},
                bytes
        );
    }

    @Test
    void dnsNameRoundTripPreservesLabels() {
        List<String> labels = List.of("service", "example", "test");
        DnsNameDom name = DnsNameDom.builder().labels(labels).build();

        byte[] bytes = RDataUtils.toByteArray(name);
        DnsNameDom decoded = RDataUtils.toDnsNameDom(bytes);

        assertEquals(labels, decoded.labels());
    }
}
