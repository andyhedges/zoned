package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import org.junit.jupiter.api.Test;

import java.net.Inet6Address;
import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AAAARecordDataDomTest {

    @Test
    void fromRejectsInvalidLength() {
        assertThrows(IllegalArgumentException.class, () -> AAAARecordDataDom.from(null));
        assertThrows(IllegalArgumentException.class, () -> AAAARecordDataDom.from(new byte[15]));
        assertThrows(IllegalArgumentException.class, () -> AAAARecordDataDom.from(new byte[17]));
    }

    @Test
    void fromParsesIpv6Bytes() {
        byte[] bytes = new byte[] {
                0x20, 0x01, 0x0d, (byte) 0xb8,
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 1
        };

        RDataDom dom = AAAARecordDataDom.from(bytes);
        AAAARecordDataDom aaaa = assertInstanceOf(AAAARecordDataDom.class, dom);

        assertArrayEquals(bytes, aaaa.getAddress().getAddress());
    }

    @Test
    void toRejectsNullAddress() {
        AAAARecordDataDom dom = AAAARecordDataDom.builder().build();
        assertThrows(IllegalArgumentException.class, dom::to);
    }

    @Test
    void toSerializesIpv6Address() throws Exception {
        byte[] bytes = new byte[] {
                0x20, 0x01, 0x0d, (byte) 0xb8,
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 1
        };
        InetAddress addr = InetAddress.getByAddress(bytes);
        Inet6Address inet6 = assertInstanceOf(Inet6Address.class, addr);

        AAAARecordDataDom dom = AAAARecordDataDom.builder().address(inet6).build();

        assertArrayEquals(bytes, dom.to());
    }

    @Test
    void roundTripPreservesAddress() throws Exception {
        byte[] bytes = new byte[] {
                0x20, 0x01, 0x0d, (byte) 0xb8,
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 1
        };
        InetAddress addr = InetAddress.getByAddress(bytes);
        Inet6Address inet6 = assertInstanceOf(Inet6Address.class, addr);

        AAAARecordDataDom original = AAAARecordDataDom.builder().address(inet6).build();
        RDataDom decoded = AAAARecordDataDom.from(original.to());
        AAAARecordDataDom parsed = assertInstanceOf(AAAARecordDataDom.class, decoded);

        assertEquals(inet6.getHostAddress(), parsed.getAddress().getHostAddress());
    }
}
