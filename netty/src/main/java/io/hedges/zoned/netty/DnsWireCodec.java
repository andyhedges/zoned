package io.hedges.zoned.netty;

import io.hedges.zoned.core.dom.*;
import io.hedges.zoned.core.dom.rdata.RDataFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class DnsWireCodec {

    private static final int HEADER_LENGTH = 12;

    private DnsWireCodec() {
    }

    public static ByteBuf encode(DnsMessageDom message, ByteBufAllocator allocator) {
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
        out.writeShort(message.header().id() & 0xFFFF);
        out.writeShort(encodeFlags(message.header()));
        out.writeShort(questions.size());
        out.writeShort(answers.size());
        out.writeShort(authorities.size());
        out.writeShort(additionals.size());

        for (DnsQuestionDom question : questions) {
            writeName(out, question.name());
            out.writeShort(toRecordType(question.recordType()));
            out.writeShort(toRecordClass(question.recordClass()));
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

    public static DnsMessageDom decode(ByteBuf buf) {
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
                .opCode(fromOpCode((flags >> 11) & 0xF))
                .authoritativeAnswer((flags & 0x0400) != 0)
                .truncation((flags & 0x0200) != 0)
                .recursionDesired((flags & 0x0100) != 0)
                .recursionAvailable((flags & 0x0080) != 0)
                .authenticatedData((flags & 0x0020) != 0)
                .checkingDisabled((flags & 0x0010) != 0)
                .responseCode(fromResponseCode(flags & 0xF))
                .build();

        List<DnsQuestionDom> questions = new ArrayList<>(qdCount);
        for (int i = 0; i < qdCount; i++) {
            DnsNameDom name = readName(buf);
            DnsRecordTypeDom type = fromRecordType(buf.readUnsignedShort());
            DnsRecordClassDom recordClass = fromRecordClass(buf.readUnsignedShort());
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

    private static List<DnsQuestionDom> safeQuestions(List<DnsQuestionDom> questions) {
        return questions == null ? List.of() : questions;
    }

    private static List<DnsResourceRecordDom> safeRecords(List<DnsResourceRecordDom> records) {
        return records == null ? List.of() : records;
    }

    private static void writeResourceRecord(ByteBuf out, DnsResourceRecordDom record) {
        writeName(out, record.name());
        out.writeShort(toRecordType(record.type()));
        out.writeShort(toRecordClass(record.recordClass()));
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

    private static int encodeFlags(DnsHeaderDom header) {
        int flags = 0;
        if (header.response()) {
            flags |= 0x8000;
        }
        flags |= (toOpCode(header.opCode()) & 0xF) << 11;
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
        flags |= (toResponseCode(header.responseCode()) & 0xF);
        return flags;
    }

    private static DnsRecordTypeDom fromRecordType(int type) {
        return switch (type) {
            case 1 -> DnsRecordTypeDom.A;
            case 2 -> DnsRecordTypeDom.NS;
            case 5 -> DnsRecordTypeDom.CNAME;
            case 6 -> DnsRecordTypeDom.SOA;
            case 12 -> DnsRecordTypeDom.PTR;
            case 15 -> DnsRecordTypeDom.MX;
            case 16 -> DnsRecordTypeDom.TXT;
            case 28 -> DnsRecordTypeDom.AAAA;
            case 33 -> DnsRecordTypeDom.SRV;
            case 41 -> DnsRecordTypeDom.OPT;
            case 43 -> DnsRecordTypeDom.DS;
            case 46 -> DnsRecordTypeDom.RRSIG;
            case 47 -> DnsRecordTypeDom.NSEC;
            case 48 -> DnsRecordTypeDom.DNSKEY;
            case 50 -> DnsRecordTypeDom.NSEC3;
            default -> throw new UnsupportedOperationException("Unsupported DNS record type: " + type);
        };
    }

    private static int toRecordType(DnsRecordTypeDom type) {
        if (type == null) {
            throw new IllegalArgumentException("record type is null");
        }
        return switch (type) {
            case A -> 1;
            case NS -> 2;
            case CNAME -> 5;
            case SOA -> 6;
            case PTR -> 12;
            case MX -> 15;
            case TXT -> 16;
            case AAAA -> 28;
            case SRV -> 33;
            case OPT -> 41;
            case DS -> 43;
            case RRSIG -> 46;
            case NSEC -> 47;
            case DNSKEY -> 48;
            case NSEC3 -> 50;
        };
    }

    private static DnsRecordClassDom fromRecordClass(int classId) {
        return switch (classId) {
            case 1 -> DnsRecordClassDom.IN;
            case 3 -> DnsRecordClassDom.CHAOS;
            case 4 -> DnsRecordClassDom.HESIOD;
            case 254 -> DnsRecordClassDom.NONE;
            case 255 -> DnsRecordClassDom.ANY;
            default -> throw new UnsupportedOperationException("Unsupported DNS record class: " + classId);
        };
    }

    private static int toRecordClass(DnsRecordClassDom recordClass) {
        if (recordClass == null) {
            throw new IllegalArgumentException("record class is null");
        }
        return switch (recordClass) {
            case IN -> 1;
            case CHAOS -> 3;
            case HESIOD -> 4;
            case NONE -> 254;
            case ANY -> 255;
        };
    }

    private static DnsOpCodeDom fromOpCode(int opcode) {
        return switch (opcode) {
            case 0 -> DnsOpCodeDom.QUERY;
            case 1 -> DnsOpCodeDom.IQUERY;
            case 2 -> DnsOpCodeDom.STATUS;
            case 4 -> DnsOpCodeDom.NOTIFY;
            case 5 -> DnsOpCodeDom.UPDATE;
            default -> throw new UnsupportedOperationException("Unsupported DNS opcode: " + opcode);
        };
    }

    private static int toOpCode(DnsOpCodeDom opCode) {
        if (opCode == null) {
            return 0;
        }
        return switch (opCode) {
            case QUERY -> 0;
            case IQUERY -> 1;
            case STATUS -> 2;
            case NOTIFY -> 4;
            case UPDATE -> 5;
        };
    }

    private static DnsResponseCodeDom fromResponseCode(int code) {
        return switch (code) {
            case 0 -> DnsResponseCodeDom.NO_ERROR;
            case 1 -> DnsResponseCodeDom.FORMAT_ERROR;
            case 2 -> DnsResponseCodeDom.SERVER_FAILURE;
            case 3 -> DnsResponseCodeDom.NAME_ERROR;
            case 4 -> DnsResponseCodeDom.NOT_IMPLEMENTED;
            case 5 -> DnsResponseCodeDom.REFUSED;
            default -> throw new UnsupportedOperationException("Unsupported DNS response code: " + code);
        };
    }

    private static int toResponseCode(DnsResponseCodeDom code) {
        if (code == null) {
            return 0;
        }
        return switch (code) {
            case NO_ERROR -> 0;
            case FORMAT_ERROR -> 1;
            case SERVER_FAILURE -> 2;
            case NAME_ERROR -> 3;
            case NOT_IMPLEMENTED -> 4;
            case REFUSED -> 5;
        };
    }

    private static List<DnsResourceRecordDom> readResourceRecords(ByteBuf buf, int count) {
        List<DnsResourceRecordDom> records = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            DnsNameDom name = readName(buf);
            DnsRecordTypeDom type = fromRecordType(buf.readUnsignedShort());
            DnsRecordClassDom recordClass = fromRecordClass(buf.readUnsignedShort());
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
