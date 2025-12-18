package io.hedges.zoned.netty;

import io.hedges.zoned.core.dom.DnsHeaderDom;
import io.hedges.zoned.core.dom.DnsMessageDom;
import io.hedges.zoned.core.dom.DnsNameDom;
import io.hedges.zoned.core.dom.DnsQuestionDom;
import io.hedges.zoned.core.dom.DnsResourceRecordDom;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.nio.charset.StandardCharsets;
import java.util.List;

public final class DnsDatagramEncoder extends MessageToMessageEncoder<UdpDnsOutbound> {

    @Override
    protected void encode(ChannelHandlerContext ctx, UdpDnsOutbound msg, List<Object> out) {
        ByteBuf buf = encodeMessage(msg.message(), ctx.alloc());
        out.add(new DatagramPacket(buf, msg.recipient()));
    }

    static ByteBuf encodeMessage(DnsMessageDom message, ByteBufAllocator allocator) {
        if (message == null) {
            throw new IllegalArgumentException("message is null");
        }
        if (message.header() == null) {
            throw new IllegalArgumentException("message.header is null");
        }

        List<DnsQuestionDom> questions = safeQuestions(message.questions());
        List<DnsResourceRecordDom> answers = safeRecords(message.answers());
        List<DnsResourceRecordDom> authorities = safeRecords(message.authorities());
        List<DnsResourceRecordDom> additionals = safeRecords(message.additionals());

        ByteBuf out = allocator.buffer();
        // Header layout (12 bytes total, byte order):
        // ID[15..0]
        // FLAGS[15..8]: QR[15], OPCODE[14..11], AA[10], TC[9], RD[8]
        // FLAGS[7..0]:  RA[7], Z/AD/CD[6..4], RCODE[3..0]
        // QDCOUNT[15..0]
        // ANCOUNT[15..0]
        // NSCOUNT[15..0]
        // ARCOUNT[15..0]
        out.writeShort(message.header().id() & 0xFFFF);
        out.writeShort(encodeFlags(message.header()));
        out.writeShort(questions.size());
        out.writeShort(answers.size());
        out.writeShort(authorities.size());
        out.writeShort(additionals.size());

        for (DnsQuestionDom question : questions) {
            writeName(out, question.name());
            out.writeShort(question.recordType().code());
            out.writeShort(question.recordClass().code());
        }

        for (DnsResourceRecordDom record : answers) {
            writeResourceRecord(out, record);
        }

        for (DnsResourceRecordDom record : authorities) {
            writeResourceRecord(out, record);
        }

        for (DnsResourceRecordDom record : additionals) {
            writeResourceRecord(out, record);
        }

        return out;
    }

    private static List<DnsQuestionDom> safeQuestions(List<DnsQuestionDom> questions) {
        return questions == null ? List.of() : questions;
    }

    private static List<DnsResourceRecordDom> safeRecords(List<DnsResourceRecordDom> records) {
        return records == null ? List.of() : records;
    }

    private static void writeResourceRecord(ByteBuf out, DnsResourceRecordDom record) {
        writeName(out, record.name());
        out.writeShort(record.type().code());
        out.writeShort(record.recordClass().code());
        out.writeInt((int) record.ttlSeconds());
        byte[] rdata = record.rdata() == null ? new byte[0] : record.rdata().to();
        out.writeShort(rdata.length);
        out.writeBytes(rdata);
    }

    private static void writeName(ByteBuf out, DnsNameDom name) {
        if (name == null || name.labels() == null) {
            throw new IllegalArgumentException("name is null");
        }
        int totalLength = 1;
        for (String label : name.labels()) {
            if (label == null) {
                throw new IllegalArgumentException("label is null");
            }
            byte[] bytes = label.getBytes(StandardCharsets.US_ASCII);
            if (bytes.length > 63) {
                throw new IllegalArgumentException("label exceeds 63 bytes: " + label);
            }
            totalLength += 1 + bytes.length;
            if (totalLength > 255) {
                throw new IllegalArgumentException("name exceeds 255 bytes");
            }
            out.writeByte(bytes.length);
            out.writeBytes(bytes);
        }
        out.writeByte(0);
    }

    private static int encodeFlags(DnsHeaderDom header) {
        int flags = 0;
        if (header.response()) {
            flags |= 0x8000;
        }
        flags |= (header.opCode() == null ? 0 : (header.opCode().code() & 0xF)) << 11;
        if (header.authoritativeAnswer()) {
            flags |= 0x0400;
        }
        if (header.truncation()) {
            flags |= 0x0200;
        }
        if (header.recursionDesired()) {
            flags |= 0x0100;
        }
        if (header.recursionAvailable()) {
            flags |= 0x0080;
        }
        if (header.authenticatedData()) {
            flags |= 0x0020;
        }
        if (header.checkingDisabled()) {
            flags |= 0x0010;
        }
        flags |= (header.responseCode() == null ? 0 : (header.responseCode().code() & 0xF));
        return flags;
    }
}
