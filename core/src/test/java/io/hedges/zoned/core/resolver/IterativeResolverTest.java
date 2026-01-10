// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.resolver;

import io.hedges.zoned.core.DnsClient;
import io.hedges.zoned.core.dom.DnsMessageDom;
import io.hedges.zoned.core.dom.DnsHeaderDom;
import io.hedges.zoned.core.dom.DnsNameDom;
import io.hedges.zoned.core.dom.DnsQuestionDom;
import io.hedges.zoned.core.dom.DnsRecordClassDom;
import io.hedges.zoned.core.dom.DnsRecordTypeDom;
import io.hedges.zoned.core.dom.DnsResourceRecordDom;
import io.hedges.zoned.core.dom.rdata.ARecordDataDom;
import io.hedges.zoned.core.dom.rdata.NsRecordDataDom;
import io.hedges.zoned.core.dom.DnsNameDomPolicy;
import org.junit.jupiter.api.Test;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IterativeResolverTest {

    @Test
    void resolvesTestExampleComARecordThroughReferrals() throws Exception {
        List<DnsMessageDom> responses = new ArrayList<>();
        responses.add(rootReferral());
        responses.add(tldReferral());
        responses.add(authoritativeAnswer());
        AtomicInteger callCount = new AtomicInteger();
        DnsClient dnsClient = (message, server, transport) -> {
            int index = callCount.getAndIncrement();
            return CompletableFuture.completedFuture(responses.get(index));
        };
        DnsQuestionDom question = DnsQuestionDom.builder()
                .name(name("test", "example", "com"))
                .recordType(DnsRecordTypeDom.A)
                .recordClass(DnsRecordClassDom.IN)
                .build();

        IterativeResolver resolver = new IterativeResolver(dnsClient);
        Resolver.Resolution resolution = resolver.resolve(question, System.currentTimeMillis())
                .toCompletableFuture()
                .join();

        assertTrue(resolution.complete());
        assertEquals(1, resolution.answers().size());
        assertEquals(3, callCount.get());
    }

    private static DnsMessageDom rootReferral() throws Exception {
        DnsResourceRecordDom ns = DnsResourceRecordDom.builder()
                .name(name("com"))
                .type(DnsRecordTypeDom.NS)
                .recordClass(DnsRecordClassDom.IN)
                .ttlSeconds(172800)
                .rdata(NsRecordDataDom.builder().nsName(name("a", "gtld-servers", "net")).build())
                .build();
        DnsResourceRecordDom glue = DnsResourceRecordDom.builder()
                .name(name("a", "gtld-servers", "net"))
                .type(DnsRecordTypeDom.A)
                .recordClass(DnsRecordClassDom.IN)
                .ttlSeconds(172800)
                .rdata(ARecordDataDom.builder().address(ipv4(192, 0, 2, 53)).build())
                .build();
        return DnsMessageDom.builder()
                .header(responseHeader())
                .authorities(List.of(ns))
                .additionals(List.of(glue))
                .build();
    }

    private static DnsMessageDom tldReferral() throws Exception {
        DnsResourceRecordDom ns = DnsResourceRecordDom.builder()
                .name(name("example", "com"))
                .type(DnsRecordTypeDom.NS)
                .recordClass(DnsRecordClassDom.IN)
                .ttlSeconds(86400)
                .rdata(NsRecordDataDom.builder().nsName(name("ns1", "example", "com")).build())
                .build();
        DnsResourceRecordDom glue = DnsResourceRecordDom.builder()
                .name(name("ns1", "example", "com"))
                .type(DnsRecordTypeDom.A)
                .recordClass(DnsRecordClassDom.IN)
                .ttlSeconds(86400)
                .rdata(ARecordDataDom.builder().address(ipv4(192, 0, 2, 54)).build())
                .build();
        return DnsMessageDom.builder()
                .header(responseHeader())
                .authorities(List.of(ns))
                .additionals(List.of(glue))
                .build();
    }

    private static DnsMessageDom authoritativeAnswer() throws Exception {
        DnsResourceRecordDom record = DnsResourceRecordDom.builder()
                .name(name("test", "example", "com"))
                .type(DnsRecordTypeDom.A)
                .recordClass(DnsRecordClassDom.IN)
                .ttlSeconds(300)
                .rdata(ARecordDataDom.builder().address(ipv4(192, 0, 2, 1)).build())
                .build();
        return DnsMessageDom.builder()
                .header(responseHeader())
                .answers(List.of(record))
                .build();
    }

    private static DnsNameDom name(String... labels) {
        return DnsNameDom.labels(DnsNameDomPolicy.Builtin.PROTOCOL, List.of(labels));
    }

    private static DnsHeaderDom responseHeader() {
        return DnsHeaderDom.builder()
                .id(0)
                .response(true)
                .build();
    }

    private static Inet4Address ipv4(int a, int b, int c, int d) throws Exception {
        return (Inet4Address) InetAddress.getByAddress(new byte[] {
                (byte) a, (byte) b, (byte) c, (byte) d
        });
    }
}