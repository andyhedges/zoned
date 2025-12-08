package io.hedges.zoned.netty;

import io.hedges.zoned.core.DnsRequestContext;
import io.hedges.zoned.core.dom.*;
import io.hedges.zoned.core.dom.DnsRecordTypeDom;
import io.hedges.zoned.core.dom.rdata.ARecordDataDom;
import io.netty.buffer.Unpooled;
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
                            .recordClass(fromNettyRecordClass(dnsQuestion.dnsClass()))
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


    public static DnsMessageDom fromNetty(DatagramDnsResponse nettyResponse) {
        int answerCount = nettyResponse.count(DnsSection.ANSWER);
        List<DnsResourceRecordDom> answers = new ArrayList<>(answerCount);
        for (int i = 0; i < answerCount; i++) {
            DnsRecord dnsAnswer = nettyResponse.recordAt(DnsSection.ANSWER, i);
            answers.add(
                    DnsResourceRecordDom.builder()
                            .name(DnsNameDom.fromFqdn(dnsAnswer.name()))
                            .type(fromNettyRecordType(dnsAnswer.type()))
                            .recordClass(fromNettyRecordClass(dnsAnswer.dnsClass()))
                            .ttlSeconds(dnsAnswer.timeToLive())
                            .rdata(ARecordDataDom.builder().build().from(new byte[]{1,1,1,1}))
                            .build()
            );
        }

        return DnsMessageDom.builder()
                .header(
                        DnsHeaderDom.builder()
                                .id(nettyResponse.id())
                                .opCode(fromNettyOpCode(nettyResponse.opCode()))
                                .recursionDesired(nettyResponse.isRecursionDesired())
                                .authoritativeAnswer(nettyResponse.isAuthoritativeAnswer())
                                .recursionAvailable(nettyResponse.isRecursionAvailable())
                                .response(true)
                                .build()
                )
                .answers(answers)
                .build();
    }

    // Outbound
    public static DatagramDnsQuery toNettyQuery(DnsMessageDom dom, InetSocketAddress sender, InetSocketAddress recipient) {
        DatagramDnsQuery nettyQuery = new DatagramDnsQuery(sender, recipient, dom.header().id());
        nettyQuery.setOpCode(toNettyDnsOpCode(dom.header().opCode()));
        nettyQuery.setRecursionDesired(dom.header().recursionDesired());

        dom.answers().stream().map(r ->
                new DefaultDnsRawRecord(
                        r.name().toFqdn(),
                        toNettyDnsRecordType(r.type()),
                        r.ttlSeconds(),
                        Unpooled.wrappedBuffer(r.rdata().to())
                )
        ).forEach(d ->
                nettyQuery.addRecord(DnsSection.ANSWER, d)
        );

        dom.questions().stream().map(r -> {
                    System.out.println("-->" + r);
                    return new DefaultDnsQuestion(
                            r.name().toFqdn(),
                            toNettyDnsRecordType(r.recordType()),
                            toNettyDnsRecordClass(r.recordClass()));
                }
        ).forEach(d ->
                nettyQuery.addRecord(DnsSection.QUESTION, d)
        );

        dom.additionals().stream()
                .map(r -> new DefaultDnsRawRecord(
                        r.name().toFqdn(),
                        toNettyDnsRecordType(r.type()),
                        r.ttlSeconds(),
                        Unpooled.wrappedBuffer(r.rdata().to())
                ))
                .forEach(rec ->
                        nettyQuery.addRecord(DnsSection.ADDITIONAL, rec)
                );

        dom.authorities().stream()
                .map(r -> new DefaultDnsRawRecord(
                        r.name().toFqdn(),
                        toNettyDnsRecordType(r.type()),
                        r.ttlSeconds(),
                        Unpooled.wrappedBuffer(r.rdata().to())
                ))
                .forEach(rec ->
                        nettyQuery.addRecord(DnsSection.AUTHORITY, rec)
                );

        return nettyQuery;
    }


    public static DatagramDnsResponse toNettyResponse(DnsMessageDom domResponse, InetSocketAddress sender, InetSocketAddress recipient) {
        DatagramDnsResponse response = new DatagramDnsResponse(sender, recipient, domResponse.header().id());

        response.setOpCode(DnsOpCode.QUERY);
        response.setCode(DnsResponseCode.SERVFAIL);
        response.setRecursionDesired(domResponse.header().recursionDesired());
        response.setRecursionAvailable(domResponse.header().recursionAvailable());
        response.setTruncated(false);
        response.setAuthoritativeAnswer(domResponse.header().authoritativeAnswer());

        for(DnsResourceRecordDom domRecord: domResponse.answers()){
            response.addRecord(DnsSection.ANSWER, toNettyDnsRecord(domRecord));
        }

        for(DnsResourceRecordDom domRecord: domResponse.authorities()){
            response.addRecord(DnsSection.AUTHORITY, toNettyDnsRecord(domRecord));
        }

        for(DnsResourceRecordDom domRecord: domResponse.additionals()){
            response.addRecord(DnsSection.ADDITIONAL, toNettyDnsRecord(domRecord));
        }

        return response;
    }

    private static DnsRecord toNettyDnsRecord(DnsResourceRecordDom domRecord) {
        return new DefaultDnsRawRecord(
                domRecord.name().toFqdn(),
                toNettyDnsRecordType(domRecord.type()),
                domRecord.ttlSeconds(),
                Unpooled.wrappedBuffer(domRecord.rdata().to())
        );
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

    private static DnsRecordClassDom fromNettyRecordClass(int classId) {
        switch (classId) {
            case (DnsRecord.CLASS_IN):
                return DnsRecordClassDom.IN;
            case (DnsRecord.CLASS_HESIOD):
                return DnsRecordClassDom.HESIOD;
            case (DnsRecord.CLASS_CHAOS):
                return DnsRecordClassDom.CHAOS;
            case (DnsRecord.CLASS_ANY):
                return DnsRecordClassDom.ANY;
            case (DnsRecord.CLASS_NONE):
                return DnsRecordClassDom.NONE;
        }
        throw new UnsupportedOperationException("Unsupported DNS Recode Class: " + classId);

    }

    private static int toNettyDnsRecordClass(DnsRecordClassDom recordClassDom) {
        return switch (recordClassDom) {
            case HESIOD -> DnsRecord.CLASS_HESIOD;
            case CHAOS -> DnsRecord.CLASS_CHAOS;
            case IN -> DnsRecord.CLASS_IN;
            case ANY -> DnsRecord.CLASS_ANY;
            case NONE -> DnsRecord.CLASS_NONE;
        };
    }

    private static DnsRecordType toNettyDnsRecordType(DnsRecordTypeDom recordTypeDom) {
        return switch (recordTypeDom) {
            case A -> DnsRecordType.A;
            case DS -> DnsRecordType.DS;
            case MX -> DnsRecordType.MX;
            case NS -> DnsRecordType.NS;
            case OPT -> DnsRecordType.OPT;
            case PTR -> DnsRecordType.PTR;
            case SOA -> DnsRecordType.SOA;
            case SRV -> DnsRecordType.SRV;
            case TXT -> DnsRecordType.TXT;
            case AAAA -> DnsRecordType.AAAA;
            case NSEC -> DnsRecordType.NSEC;
            case CNAME -> DnsRecordType.CNAME;
            case NSEC3 -> DnsRecordType.NSEC3;
            case RRSIG -> DnsRecordType.RRSIG;
            case DNSKEY -> DnsRecordType.DNSKEY;
        };
    }

    private static DnsOpCode toNettyDnsOpCode(DnsOpCodeDom opCodeDom) {
        return switch (opCodeDom) {
            case QUERY -> DnsOpCode.QUERY;
            case IQUERY -> DnsOpCode.IQUERY;
            case NOTIFY -> DnsOpCode.NOTIFY;
            case STATUS -> DnsOpCode.STATUS;
            case UPDATE -> DnsOpCode.UPDATE;
        };
    }
}
