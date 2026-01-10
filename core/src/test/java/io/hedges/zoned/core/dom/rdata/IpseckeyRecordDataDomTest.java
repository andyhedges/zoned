// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.DnsNameDom;
import io.hedges.zoned.core.dom.RDataDom;
import io.hedges.zoned.core.dom.DnsNameDomPolicy;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IpseckeyRecordDataDomTest {

    @Test
    void fromRejectsInvalidLength() {
        assertThrows(IllegalArgumentException.class, () -> IpseckeyRecordDataDom.from(null, null));
        assertThrows(IllegalArgumentException.class, () -> IpseckeyRecordDataDom.from(new byte[2], null));
    }

    @Test
    void fromRejectsUnsupportedGatewayType() {
        byte[] rdata = new byte[] {0, 4, 1};
        assertThrows(IllegalArgumentException.class, () -> IpseckeyRecordDataDom.from(rdata, null));
    }

    @Test
    void fromRejectsTruncatedGateway() {
        assertThrows(IllegalArgumentException.class, () -> IpseckeyRecordDataDom.from(new byte[] {0, 1, 1, 1}, null));
        assertThrows(IllegalArgumentException.class, () -> IpseckeyRecordDataDom.from(new byte[] {0, 2, 1, 1, 2, 3}, null));
    }

    @Test
    void fromParsesIpv4Gateway() {
        byte[] rdata = new byte[] {10, 1, 1, 1, 2, 3, 4, 9, 8};
        RDataDom dom = IpseckeyRecordDataDom.from(rdata, null);
        IpseckeyRecordDataDom ipseckey = assertInstanceOf(IpseckeyRecordDataDom.class, dom);

        assertEquals(10, ipseckey.precedence());
        assertEquals(1, ipseckey.gatewayType());
        assertEquals(1, ipseckey.algorithm());
        assertArrayEquals(new byte[] {1, 2, 3, 4}, ipseckey.gateway());
        assertArrayEquals(new byte[] {9, 8}, ipseckey.publicKey());
    }

    @Test
    void fromParsesNameGateway() {
        DnsNameDom name = DnsNameDom.labels(DnsNameDomPolicy.Builtin.PROTOCOL, List.of("gw", "example", "test"));
        byte[] nameBytes = RDataUtils.toByteArray(name);
        byte[] rdata = new byte[3 + nameBytes.length + 2];
        int idx = 0;
        rdata[idx++] = 5;
        rdata[idx++] = 3;
        rdata[idx++] = 1;
        System.arraycopy(nameBytes, 0, rdata, idx, nameBytes.length);
        idx += nameBytes.length;
        rdata[idx++] = 7;
        rdata[idx] = 6;

        RDataDom dom = IpseckeyRecordDataDom.from(rdata, null);
        IpseckeyRecordDataDom ipseckey = assertInstanceOf(IpseckeyRecordDataDom.class, dom);

        assertEquals(5, ipseckey.precedence());
        assertEquals(3, ipseckey.gatewayType());
        assertEquals(1, ipseckey.algorithm());
        assertArrayEquals(nameBytes, ipseckey.gateway());
        assertArrayEquals(new byte[] {7, 6}, ipseckey.publicKey());
    }

    @Test
    void toRejectsInvalidFields() {
        IpseckeyRecordDataDom missingGateway = IpseckeyRecordDataDom.builder()
                .precedence(1)
                .gatewayType(1)
                .algorithm(1)
                .publicKey(new byte[] {1})
                .build();
        IpseckeyRecordDataDom badGatewayType = IpseckeyRecordDataDom.builder()
                .precedence(1)
                .gatewayType(4)
                .algorithm(1)
                .gateway(new byte[0])
                .publicKey(new byte[] {1})
                .build();
        IpseckeyRecordDataDom badPrecedence = IpseckeyRecordDataDom.builder()
                .precedence(256)
                .gatewayType(1)
                .algorithm(1)
                .gateway(new byte[] {1, 2, 3, 4})
                .publicKey(new byte[] {1})
                .build();
        IpseckeyRecordDataDom badAlgorithm = IpseckeyRecordDataDom.builder()
                .precedence(1)
                .gatewayType(1)
                .algorithm(256)
                .gateway(new byte[] {1, 2, 3, 4})
                .publicKey(new byte[] {1})
                .build();
        IpseckeyRecordDataDom badGatewayLength = IpseckeyRecordDataDom.builder()
                .precedence(1)
                .gatewayType(1)
                .algorithm(1)
                .gateway(new byte[] {1, 2, 3})
                .publicKey(new byte[] {1})
                .build();
        IpseckeyRecordDataDom missingPublicKey = IpseckeyRecordDataDom.builder()
                .precedence(1)
                .gatewayType(0)
                .algorithm(1)
                .gateway(new byte[0])
                .build();

        assertThrows(IllegalArgumentException.class, missingGateway::to);
        assertThrows(IllegalArgumentException.class, badGatewayType::to);
        assertThrows(IllegalArgumentException.class, badPrecedence::to);
        assertThrows(IllegalArgumentException.class, badAlgorithm::to);
        assertThrows(IllegalArgumentException.class, badGatewayLength::to);
        assertThrows(IllegalArgumentException.class, missingPublicKey::to);
    }

    @Test
    void toSerializesRdata() {
        IpseckeyRecordDataDom dom = IpseckeyRecordDataDom.builder()
                .precedence(10)
                .gatewayType(1)
                .algorithm(1)
                .gateway(new byte[] {1, 2, 3, 4})
                .publicKey(new byte[] {9, 8})
                .build();

        assertArrayEquals(new byte[] {10, 1, 1, 1, 2, 3, 4, 9, 8}, dom.to());
    }

    @Test
    void roundTripPreservesFields() {
        DnsNameDom name = DnsNameDom.labels(DnsNameDomPolicy.Builtin.PROTOCOL, List.of("gw", "example", "test"));
        byte[] nameBytes = RDataUtils.toByteArray(name);
        IpseckeyRecordDataDom original = IpseckeyRecordDataDom.builder()
                .precedence(3)
                .gatewayType(3)
                .algorithm(1)
                .gateway(nameBytes)
                .publicKey(new byte[] {5, 6})
                .build();

        RDataDom decoded = IpseckeyRecordDataDom.from(original.to(), null);
        IpseckeyRecordDataDom parsed = assertInstanceOf(IpseckeyRecordDataDom.class, decoded);

        assertEquals(3, parsed.precedence());
        assertEquals(3, parsed.gatewayType());
        assertEquals(1, parsed.algorithm());
        assertArrayEquals(nameBytes, parsed.gateway());
        assertArrayEquals(new byte[] {5, 6}, parsed.publicKey());
    }
}