package io.hedges.zoned.netty;

import io.hedges.zoned.core.dom.DnsHeaderDom;
import io.hedges.zoned.core.dom.DnsMessageDom;
import io.hedges.zoned.core.dom.DnsNameDom;
import io.hedges.zoned.core.dom.DnsQuestionDom;
import io.hedges.zoned.core.dom.DnsRecordClassDom;
import io.hedges.zoned.core.dom.DnsRecordTypeDom;
import io.hedges.zoned.core.dom.DnsResourceRecordDom;
import io.hedges.zoned.core.dom.rdata.RDataFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class DnsDatagramDecoder extends SimpleChannelInboundHandler<DatagramPacket> {

    private static final int HEADER_LENGTH = 12;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {
        ctx.fireChannelRead(new UdpDnsInbound(
                decodeMessage(packet.content()),
                packet.sender()));
    }

    static DnsMessageDom decodeMessage(ByteBuf buf) {
        if (buf == null) {
            throw new IllegalArgumentException("buf is null");
        }
        if (buf.readableBytes() < HEADER_LENGTH) {
            throw new IllegalArgumentException("DNS message too short: " + buf.readableBytes());
        }

        int id = buf.readUnsignedShort();
        int flags = buf.readUnsignedShort();
        int qdCount = buf.readUnsignedShort();
        int anCount = buf.readUnsignedShort();
        int nsCount = buf.readUnsignedShort();
        int arCount = buf.readUnsignedShort();

        DnsHeaderDom header = DnsHeaderDom.builder()
                .id(id)
                .response((flags & 0x8000) != 0)
                .opCode(DnsWireMappings.opCodeFromCode((flags >> 11) & 0xF))
                .authoritativeAnswer((flags & 0x0400) != 0)
                .truncation((flags & 0x0200) != 0)
                .recursionDesired((flags & 0x0100) != 0)
                .recursionAvailable((flags & 0x0080) != 0)
                .authenticatedData((flags & 0x0020) != 0)
                .checkingDisabled((flags & 0x0010) != 0)
                .responseCode(DnsWireMappings.responseCodeFromCode(flags & 0xF))
                .build();

        List<DnsQuestionDom> questions = new ArrayList<>(qdCount);
        for (int i = 0; i < qdCount; i++) {
            DnsNameDom name = readName(buf);
            DnsRecordTypeDom type = DnsWireMappings.recordTypeFromCode(buf.readUnsignedShort());
            DnsRecordClassDom recordClass = DnsWireMappings.recordClassFromCode(buf.readUnsignedShort());
            questions.add(DnsQuestionDom.builder()
                    .name(name)
                    .recordType(type)
                    .recordClass(recordClass)
                    .build());
        }

        List<DnsResourceRecordDom> answers = readResourceRecords(buf, anCount);
        skipResourceRecords(buf, nsCount);
        skipResourceRecords(buf, arCount);

        return DnsMessageDom.builder()
                .header(header)
                .questions(questions)
                .answers(answers)
                .authorities(List.of())
                .additionals(List.of())
                .build();
    }

    private static DnsNameDom readName(ByteBuf buf) {
        NameParseResult result = readName(buf, buf.readerIndex(), new HashSet<>());
        buf.readerIndex(result.endIndex());
        return DnsNameDom.builder().labels(result.labels()).build();
    }

    private static NameParseResult readName(ByteBuf buf, int start, Set<Integer> visitedOffsets) {
        int idx = start;
        int endIndex = -1;
        boolean jumped = false;
        int limit = buf.writerIndex();
        List<String> labels = new ArrayList<>();

        while (true) {
            if (idx >= limit) {
                throw new IllegalArgumentException("name exceeds buffer bounds");
            }
            int len = buf.getUnsignedByte(idx);
            if ((len & 0xC0) == 0xC0) {
                if (idx + 1 >= limit) {
                    throw new IllegalArgumentException("truncated compression pointer");
                }
                int pointer = ((len & 0x3F) << 8) | buf.getUnsignedByte(idx + 1);
                if (!visitedOffsets.add(pointer)) {
                    throw new IllegalArgumentException("compression pointer loop");
                }
                if (!jumped) {
                    endIndex = idx + 2;
                    jumped = true;
                }
                idx = pointer;
                continue;
            }

            if (len == 0) {
                if (!jumped) {
                    endIndex = idx + 1;
                }
                break;
            }

            if (len > 63) {
                throw new IllegalArgumentException("label exceeds 63 bytes");
            }
            idx++;
            if (idx + len > limit) {
                throw new IllegalArgumentException("label exceeds buffer bounds");
            }
            byte[] labelBytes = new byte[len];
            buf.getBytes(idx, labelBytes);
            labels.add(new String(labelBytes, StandardCharsets.US_ASCII));
            idx += len;
        }

        if (endIndex < 0) {
            throw new IllegalStateException("invalid name termination");
        }

        return new NameParseResult(labels, endIndex);
    }

    private static List<DnsResourceRecordDom> readResourceRecords(ByteBuf buf, int count) {
        List<DnsResourceRecordDom> records = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            DnsNameDom name = readName(buf);
            DnsRecordTypeDom type = DnsWireMappings.recordTypeFromCode(buf.readUnsignedShort());
            DnsRecordClassDom recordClass = DnsWireMappings.recordClassFromCode(buf.readUnsignedShort());
            long ttl = buf.readUnsignedInt();
            int rdLength = buf.readUnsignedShort();
            if (rdLength > buf.readableBytes()) {
                throw new IllegalArgumentException("RDATA length exceeds buffer: " + rdLength);
            }
            byte[] rdataBytes = new byte[rdLength];
            buf.readBytes(rdataBytes);
            records.add(DnsResourceRecordDom.builder()
                    .name(name)
                    .type(type)
                    .recordClass(recordClass)
                    .ttlSeconds(ttl)
                    .rdata(RDataFactory.fromBytes(type, rdataBytes))
                    .build());
        }
        return records;
    }

    private static void skipResourceRecords(ByteBuf buf, int count) {
        for (int i = 0; i < count; i++) {
            readName(buf);
            if (buf.readableBytes() < 10) {
                throw new IllegalArgumentException("record header exceeds buffer");
            }
            buf.skipBytes(2); // type
            buf.skipBytes(2); // class
            buf.skipBytes(4); // ttl
            int rdLength = buf.readUnsignedShort();
            if (rdLength > buf.readableBytes()) {
                throw new IllegalArgumentException("RDATA length exceeds buffer: " + rdLength);
            }
            buf.skipBytes(rdLength);
        }
    }

    private record NameParseResult(List<String> labels, int endIndex) {
    }
}
