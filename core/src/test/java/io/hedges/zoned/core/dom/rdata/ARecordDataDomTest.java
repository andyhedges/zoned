package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import org.junit.jupiter.api.Test;

import java.net.Inet4Address;
import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ARecordDataDomTest {

    @Test
    void fromRejectsInvalidLength() {
        assertThrows(IllegalArgumentException.class, () -> ARecordDataDom.from(null));
        assertThrows(IllegalArgumentException.class, () -> ARecordDataDom.from(new byte[3]));
        assertThrows(IllegalArgumentException.class, () -> ARecordDataDom.from(new byte[5]));
    }

    @Test
    void fromParsesIpv4Bytes() {
        byte[] bytes = new byte[] {(byte) 192, 0, 2, 7};

        RDataDom dom = ARecordDataDom.from(bytes);
        ARecordDataDom a = assertInstanceOf(ARecordDataDom.class, dom);

        assertArrayEquals(bytes, a.address().getAddress());
    }

    @Test
    void toRejectsNullAddress() {
        ARecordDataDom dom = ARecordDataDom.builder().build();
        assertThrows(IllegalArgumentException.class, dom::to);
    }

    @Test
    void toSerializesIpv4Address() throws Exception {
        byte[] bytes = new byte[] {(byte) 192, 0, 2, 7};
        InetAddress addr = InetAddress.getByAddress(bytes);
        Inet4Address inet4 = assertInstanceOf(Inet4Address.class, addr);

        ARecordDataDom dom = ARecordDataDom.builder().address(inet4).build();

        assertArrayEquals(bytes, dom.to());
    }

    @Test
    void roundTripPreservesAddress() throws Exception {
        byte[] bytes = new byte[] {(byte) 192, 0, 2, 7};
        InetAddress addr = InetAddress.getByAddress(bytes);
        Inet4Address inet4 = assertInstanceOf(Inet4Address.class, addr);

        ARecordDataDom original = ARecordDataDom.builder().address(inet4).build();
        RDataDom decoded = ARecordDataDom.from(original.to());
        ARecordDataDom parsed = assertInstanceOf(ARecordDataDom.class, decoded);

        assertEquals(inet4.getHostAddress(), parsed.address().getHostAddress());
    }
}
