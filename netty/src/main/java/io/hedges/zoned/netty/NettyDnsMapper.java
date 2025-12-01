package io.hedges.zoned.netty;

import io.hedges.zoned.core.DnsRequestContext;
import io.hedges.zoned.core.dom.*;
import io.hedges.zoned.core.dom.DnsRecordTypeDom;
import io.netty.handler.codec.dns.*;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class NettyDnsMapper {

    // Inbound
    public static DnsRequestContext fromNetty(DatagramDnsQuery nettyQuery, Transport transport) {
        int questionCount = nettyQuery.count(DnsSection.QUESTION);
        List<DnsQuestionDom> questions = new ArrayList<>(questionCount);
        for (int i = 0; i < questionCount; i++) {
            DnsQuestion dnsQuestion = nettyQuery.recordAt(DnsSection.QUESTION, i);
            questions.add(
                    DnsQuestionDom
                            .builder()
                            .name(
                                    DnsNameDom.fromFqdn(
                                            dnsQuestion.name()
                                    )
                            )
                            .recordType(fromNettyRecordType(dnsQuestion.type()))
                            .build()
            );
        }
        return DnsRequestContext
                .builder()
                .query(
                        DnsMessageDom
                                .builder()
                                .questions(
                                        questions
                                )
                                .header(
                                        DnsHeaderDom
                                                .builder()
                                                .id(nettyQuery.id())
                                                .opCode(fromNettyOpCode(nettyQuery.opCode()))
                                                .recursionDesired(nettyQuery.isRecursionDesired())
                                                .build()
                                )
                                .build()
                )
                .clientAddress(nettyQuery.sender())
                .transport(transport)
                .receivedAt(Instant.now())
                .build();
    }

    public static DnsRequestContext fromNetty(DatagramDnsResponse nettyResponse) {
        return null;
    }

    // Outbound
    public static DatagramDnsQuery toNettyQuery(DnsMessageDom dom, InetSocketAddress sender, InetSocketAddress recipient) {
        return null;
    }

    public static DatagramDnsResponse toNettyResponse(DnsMessageDom dom, InetSocketAddress sender, InetSocketAddress recipient) {
        return null;
    }

    private static DnsRecordTypeDom fromNettyRecordType(DnsRecordType nettyRecordType) {
        if (nettyRecordType == null) {
            throw new IllegalArgumentException("nettyRecordType is null");
        }
        try {
            return DnsRecordTypeDom.valueOf(nettyRecordType.name());
        } catch (IllegalArgumentException ex) {
            throw new UnsupportedOperationException("Unsupported Netty DnsRecordType: " + nettyRecordType.name(), ex);
        }
    }

    private static DnsOpCodeDom fromNettyOpCode(DnsOpCode nettyOpCode) {
        if (nettyOpCode == null) {
            throw new IllegalArgumentException("nettyOpCode is null");
        }

        // Identity comparison because Netty uses singletons for known opcodes
        if (nettyOpCode == DnsOpCode.QUERY) {
            return DnsOpCodeDom.QUERY;
        }
        if (nettyOpCode == DnsOpCode.IQUERY) {
            return DnsOpCodeDom.IQUERY;
        }
        if (nettyOpCode == DnsOpCode.STATUS) {
            return DnsOpCodeDom.STATUS;
        }
        if (nettyOpCode == DnsOpCode.NOTIFY) {
            return DnsOpCodeDom.NOTIFY;
        }
        if (nettyOpCode == DnsOpCode.UPDATE) {
            return DnsOpCodeDom.UPDATE;
        }

        throw new UnsupportedOperationException("Unsupported DNS OpCode: " + nettyOpCode.toString());
    }
}
