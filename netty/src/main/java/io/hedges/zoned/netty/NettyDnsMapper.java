package io.hedges.zoned.netty;

import io.hedges.zoned.core.domain.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import io.netty.buffer.ByteBufHolder;
import io.netty.handler.codec.dns.*;


final class NettyDnsMapper {

    private NettyDnsMapper() {}

    static DnsRequestContextDom toDomain(DatagramDnsQuery query) {
        List<DnsQuestionDom> questions = new ArrayList<>();
        int qCount = query.count(DnsSection.QUESTION);
        for (int i = 0; i < qCount; i++) {
            DnsQuestion q = query.recordAt(DnsSection.QUESTION, i);
            questions.add(new DnsQuestionDom(
                    new DnsName(q.name()),
                    mapTypeBack(q.type()),
                    mapClassBack(q.dnsClass())
            ));
        }

        int ansCount = query.count(DnsSection.ANSWER);
        for (int i = 0; i < ansCount; i++) {
            //TODO
        }

        int authCount = query.count(DnsSection.AUTHORITY);
        for (int i = 0; i < authCount; i++){
            //TODO
        }

        int addAcount = query.count(DnsSection.ADDITIONAL);
        List<DnsRecordDom> additionals = new ArrayList<>();
        for (int i = 0; i < addAcount; i++){
            DnsRecord d = query.recordAt(DnsSection.ADDITIONAL, i);
            if(d instanceof DnsOptPseudoRecord) {
                System.out.println("TYPE:" + d.getClass().getCanonicalName());
            }
            if(d.type() == DnsRecordType.OPT) {
                System.out.println("OPT:" + d.getClass().getCanonicalName());
            }
            ByteBuf dataBuf = null;
            byte[] rdata;
            if (d instanceof ByteBufHolder holder) {
                dataBuf = holder.content();
                rdata = ByteBufUtil.getBytes(dataBuf.slice());
            } else {
                rdata = new byte[0];
            }
            System.out.printf("---> name=%s, type=%s, dnsClass=%s, ttl=%d\n", d.name(), d.type(), d.dnsClass(), d.timeToLive());
            additionals.add(new DnsRecordDom(
                    new DnsName(d.name()),
                    mapTypeBack(d.type()),
                    mapClassBack(d.dnsClass()),
                    d.timeToLive(),
                    rdata
            ));
        }

        DnsMessageDom domMsg = new DnsMessageDom(
                query.id(),
                query.isRecursionDesired(),
                false,
                false,
                DnsResponseCodeDom.NO_ERROR,
                questions,
                List.of(),
                List.of(),
                additionals
        );

        InetSocketAddress client = query.sender();

        return new DnsRequestContextDom(
                client,
                Transport.UDP,
                domMsg,
                Instant.now()
        );
    }

    static DatagramDnsResponse toNetty(DnsMessageDom dom, DatagramDnsQuery original) {
        DatagramDnsResponse resp = new DatagramDnsResponse(
                original.recipient(),
                original.sender(),
                dom.getId()
        );

        resp.setCode(mapRcode(dom.getRcode()));
        resp.setRecursionAvailable(dom.isRecursionAvailable());
        if (dom.isAuthoritativeAnswer()) {
            resp.setAuthoritativeAnswer(true);
        }
        if (dom.isRecursionDesired()) {
            resp.setRecursionDesired(true);
        }

        // Questions
        for (DnsQuestionDom q : dom.getQuestions()) {
            resp.addRecord(DnsSection.QUESTION,
                    new DefaultDnsQuestion(
                            q.getName().value(),
                            mapType(q.getType()),
                            mapClass(q.getRecordClass())
                    ));
        }

        dom.getAnswers().forEach(r ->
                resp.addRecord(DnsSection.ANSWER, toNettyRecord(r)));
        dom.getAuthorities().forEach(r ->
                resp.addRecord(DnsSection.AUTHORITY, toNettyRecord(r)));
        dom.getAdditionals().forEach(r ->
                resp.addRecord(DnsSection.ADDITIONAL, toNettyRecord(r)));

        return resp;
    }

    static DnsMessageDom toDomainResponse(DatagramDnsResponse msg, int originalId) {
        List<DnsQuestionDom> questions = new ArrayList<>();
        List<DnsRecordDom> answers = new ArrayList<>();
        List<DnsRecordDom> authorities = new ArrayList<>();
        List<DnsRecordDom> additionals = new ArrayList<>();

        int qCount = msg.count(DnsSection.QUESTION);
        for (int i = 0; i < qCount; i++) {
            DnsQuestion q = msg.recordAt(DnsSection.QUESTION, i);
            questions.add(new DnsQuestionDom(
                    new DnsName(q.name()),
                    mapTypeBack(q.type()),
                    mapClassBack(q.dnsClass())
            ));
        }


        copySection(msg, DnsSection.ANSWER, answers);
        copySection(msg, DnsSection.AUTHORITY, authorities);
        copySection(msg, DnsSection.ADDITIONAL, additionals);

        return new DnsMessageDom(
                originalId,
                msg.isRecursionDesired(),
                msg.isRecursionAvailable(),
                msg.isAuthoritativeAnswer(),
                mapRcodeBack(msg.code()),
                questions,
                answers,
                authorities,
                additionals
        );
    }

    private static void copySection(DnsMessage msg,
                                    DnsSection sec,
                                    List<DnsRecordDom> out) {
        int count = msg.count(sec);
        for (int i = 0; i < count; i++) {
            DnsRecord rec = msg.recordAt(sec, i);
            if (rec == null) {
                continue;
            }
            ByteBuf dataBuf = null;
            byte[] rdata;

            //System.out.println(sec.name() + "----> " + rec.getClass().getCanonicalName());

            if (rec instanceof ByteBufHolder holder) {
                dataBuf = holder.content();
                rdata = ByteBufUtil.getBytes(dataBuf.slice());
            } else {
                rdata = new byte[0];
            }
            out.add(new DnsRecordDom(
                    new DnsName(rec.name()),
                    mapTypeBack(rec.type()),
                    mapClassBack(rec.dnsClass()),
                    rec.timeToLive(),
                    rdata
            ));
        }
    }

    private static DnsRecord toNettyRecord(DnsRecordDom r) {
        ByteBuf buf = Unpooled.wrappedBuffer(r.getRdata());
        return new DefaultDnsRawRecord(
                r.getName().value(),
                mapType(r.getType()),
                mapClass(r.getRecordClass()),
                r.getTtlSeconds(),
                buf
        );
    }

    // Type mapping
    static DnsRecordType mapType(DnsRecordTypeDom t) {
        return switch (t) {
            case A -> DnsRecordType.A;
            case AAAA -> DnsRecordType.AAAA;
            case CNAME -> DnsRecordType.CNAME;
            case NS -> DnsRecordType.NS;
            case MX -> DnsRecordType.MX;
            case TXT -> DnsRecordType.TXT;
            case SOA -> DnsRecordType.SOA;
            case PTR -> DnsRecordType.PTR;
            case SRV -> DnsRecordType.SRV;
            case ANY -> DnsRecordType.ANY;
        };
    }

    static DnsRecordTypeDom mapTypeBack(DnsRecordType t) {
        return switch (t.name()) {
            case "A" -> DnsRecordTypeDom.A;
            case "AAAA" -> DnsRecordTypeDom.AAAA;
            case "CNAME" -> DnsRecordTypeDom.CNAME;
            case "NS" -> DnsRecordTypeDom.NS;
            case "MX" -> DnsRecordTypeDom.MX;
            case "TXT" -> DnsRecordTypeDom.TXT;
            case "SOA" -> DnsRecordTypeDom.SOA;
            case "PTR" -> DnsRecordTypeDom.PTR;
            case "SRV" -> DnsRecordTypeDom.SRV;
            case "ANY" -> DnsRecordTypeDom.ANY;
            default -> DnsRecordTypeDom.ANY;
        };
    }

    // Class mapping
    static int mapClass(DnsRecordClassDom c) {
        return switch (c) {
            case IN -> 1;
            case CH -> 3;
            case HS -> 4;
            case ANY -> 255;
        };
    }

    static DnsRecordClassDom mapClassBack(int dnsClass) {
        return switch (dnsClass) {
            case 1 -> DnsRecordClassDom.IN;
            case 3 -> DnsRecordClassDom.CH;
            case 4 -> DnsRecordClassDom.HS;
            case 255 -> DnsRecordClassDom.ANY;
            default -> DnsRecordClassDom.IN;
        };
    }

    // RCODE mapping
    static DnsResponseCode mapRcode(DnsResponseCodeDom r) {
        return switch (r) {
            case NO_ERROR -> DnsResponseCode.NOERROR;
            case FORMAT_ERROR -> DnsResponseCode.FORMERR;
            case SERVER_FAILURE -> DnsResponseCode.SERVFAIL;
            case NXDOMAIN -> DnsResponseCode.NXDOMAIN;
            case NOT_IMPLEMENTED -> DnsResponseCode.NOTIMP;
            case REFUSED -> DnsResponseCode.REFUSED;
        };
    }

    static DnsResponseCodeDom mapRcodeBack(DnsResponseCode r) {
        if (r.equals(DnsResponseCode.NOERROR)) {
            return DnsResponseCodeDom.NO_ERROR;
        } else if (r.equals(DnsResponseCode.FORMERR)) {
            return DnsResponseCodeDom.FORMAT_ERROR;
        } else if (r.equals(DnsResponseCode.SERVFAIL)){
            return DnsResponseCodeDom.SERVER_FAILURE;
        } else if (r.equals(DnsResponseCode.NXDOMAIN)){
            return DnsResponseCodeDom.NXDOMAIN;
        } else if (r.equals(DnsResponseCode.NOTIMP)){
            return DnsResponseCodeDom.NOT_IMPLEMENTED;
        } else if (r.equals(DnsResponseCode.REFUSED)){
            return DnsResponseCodeDom.REFUSED;
        }
        throw new IllegalArgumentException("Doh!");
    }
}
