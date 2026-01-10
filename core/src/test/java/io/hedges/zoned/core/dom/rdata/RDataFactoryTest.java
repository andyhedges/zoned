// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.NameResolver;
import io.hedges.zoned.core.dom.DnsNameDom;
import io.hedges.zoned.core.dom.DnsRecordTypeDom;
import io.hedges.zoned.core.dom.RDataDom;
import io.hedges.zoned.core.dom.DnsNameDomPolicy;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RDataFactoryTest {

    @Test
    void fromBytesUsesResolverForCname() {
        NameResolver resolver = offset -> DnsNameDom.labels(DnsNameDomPolicy.Builtin.PROTOCOL, List.of("alias", "example"));
        byte[] rdata = new byte[] {(byte) 0xC0, 0x10};

        RDataDom dom = RDataFactory.fromBytes(DnsRecordTypeDom.CNAME, rdata, resolver);
        CnameRecordDataDom cname = assertInstanceOf(CnameRecordDataDom.class, dom);

        assertEquals(List.of("alias", "example"), cname.cname().labelStrings());
    }

    @Test
    void fromBytesUsesResolverForNs() {
        NameResolver resolver = offset -> DnsNameDom.labels(DnsNameDomPolicy.Builtin.PROTOCOL, List.of("ns1", "example"));
        byte[] rdata = new byte[] {(byte) 0xC0, 0x20};

        RDataDom dom = RDataFactory.fromBytes(DnsRecordTypeDom.NS, rdata, resolver);
        NsRecordDataDom ns = assertInstanceOf(NsRecordDataDom.class, dom);

        assertEquals(List.of("ns1", "example"), ns.nsName().labelStrings());
    }

    @Test
    void fromBytesUsesResolverForMx() {
        NameResolver resolver = offset -> DnsNameDom.labels(DnsNameDomPolicy.Builtin.PROTOCOL, List.of("mail", "example"));
        byte[] rdata = new byte[] {0, 5, (byte) 0xC0, 0x10};

        RDataDom dom = RDataFactory.fromBytes(DnsRecordTypeDom.MX, rdata, resolver);
        MxRecordDataDom mx = assertInstanceOf(MxRecordDataDom.class, dom);

        assertEquals(5, mx.preference());
        assertEquals(List.of("mail", "example"), mx.exchange().labelStrings());
    }

    @Test
    void fromBytesRejectsNullType() {
        assertThrows(IllegalArgumentException.class, () -> RDataFactory.fromBytes(null, new byte[] {0}));
    }
}