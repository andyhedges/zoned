package io.hedges.zoned.core;

import io.hedges.zoned.core.dom.DnsHeaderDom;
import io.hedges.zoned.core.dom.DnsMessageDom;
import io.hedges.zoned.core.dom.DnsNameDom;
import io.hedges.zoned.core.dom.DnsOpCodeDom;
import io.hedges.zoned.core.dom.DnsQuestionDom;
import io.hedges.zoned.core.dom.DnsRecordClassDom;
import io.hedges.zoned.core.dom.DnsRecordTypeDom;
import io.hedges.zoned.core.dom.DnsResourceRecordDom;
import io.hedges.zoned.core.dom.DnsResponseCodeDom;
import io.hedges.zoned.core.dom.rdata.OptRecordDataDom;
import io.hedges.zoned.core.dom.rdata.RDataFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class DnsMessageDecoder {

    private static final int HEADER_LENGTH = 12;

    private DnsMessageDecoder() {
    }

    public static DnsMessageDom decode(DnsWireReader reader) {
        if (reader == null) {
            throw new IllegalArgumentException("reader is null");
        }
        if (reader.remaining() < HEADER_LENGTH) {
            throw new IllegalArgumentException("DNS message too short: " + reader.remaining());
        }

        int id = reader.readU16();
        int flags = reader.readU16();
        int qdCount = reader.readU16();
        int anCount = reader.readU16();
        int nsCount = reader.readU16();
        int arCount = reader.readU16();

        DnsHeaderDom header = DnsHeaderDom.builder()
                                          .id(id)
                                          .response((flags & 0x8000) != 0)
                                          .opCode(DnsOpCodeDom.fromCode((flags >> 11) & 0xF))
                                          .authoritativeAnswer((flags & 0x0400) != 0)
                                          .truncation((flags & 0x0200) != 0)
                                          .recursionDesired((flags & 0x0100) != 0)
                                          .recursionAvailable((flags & 0x0080) != 0)
                                          .authenticatedData((flags & 0x0020) != 0)
                                          .checkingDisabled((flags & 0x0010) != 0)
                                          .responseCode(DnsResponseCodeDom.fromCode(flags & 0xF))
                                          .build();

        NameResolver resolver = offset -> resolveNameAt(reader, offset);

        List<DnsQuestionDom> questions = new ArrayList<>(qdCount);
        for (int i = 0; i < qdCount; i++) {
            DnsNameDom name = readName(reader);
            DnsRecordTypeDom type = DnsRecordTypeDom.fromCode(reader.readU16());
            DnsRecordClassDom recordClass = DnsRecordClassDom.fromCode(reader.readU16());
            questions.add(DnsQuestionDom.builder()
                    .name(name)
                    .recordType(type)
                    .recordClass(recordClass)
                    .build());
        }

        List<DnsResourceRecordDom> answers = readResourceRecords(reader, anCount, resolver);
        List<DnsResourceRecordDom> authorities = readResourceRecords(reader, nsCount, resolver);
        List<DnsResourceRecordDom> additionals = readResourceRecords(reader, arCount, resolver);

        return DnsMessageDom.builder()
                .header(header)
                .questions(questions)
                .answers(answers)
                .authorities(authorities)
                .additionals(additionals)
                .build();
    }

    private static List<DnsResourceRecordDom> readResourceRecords(DnsWireReader reader, int count, NameResolver resolver) {
        List<DnsResourceRecordDom> records = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            records.add(readResourceRecord(reader, resolver));
        }
        return records;
    }

    private static DnsResourceRecordDom readResourceRecord(DnsWireReader reader, NameResolver resolver) {
        DnsNameDom name = readName(reader);
        DnsRecordTypeDom type = DnsRecordTypeDom.fromCode(reader.readU16());
        int classCode = reader.readU16();
        DnsRecordClassDom recordClass = type == DnsRecordTypeDom.OPT ? null : DnsRecordClassDom.fromCode(classCode);
        long ttl = reader.readU32();
        int rdLength = reader.readU16();
        if (rdLength > reader.remaining()) {
            throw new IllegalArgumentException("RDATA length exceeds buffer: " + rdLength);
        }
        byte[] rdataBytes = new byte[rdLength];
        reader.readBytes(rdataBytes, 0, rdLength);
        if (type == DnsRecordTypeDom.OPT) {
            return DnsResourceRecordDom.builder()
                    .name(name)
                    .type(type)
                    .recordClass(recordClass)
                    .ttlSeconds(ttl)
                    .rdata(OptRecordDataDom.from(rdataBytes, classCode, ttl))
                    .build();
        }
        return DnsResourceRecordDom.builder()
                .name(name)
                .type(type)
                .recordClass(recordClass)
                .ttlSeconds(ttl)
                .rdata(RDataFactory.fromWire(type, rdataBytes, resolver))
                .build();
    }

    private static DnsNameDom readName(DnsWireReader reader) {
        NameParseResult result = readName(reader, reader.position(), new HashSet<>());
        reader.position(result.endIndex());
        return DnsNameDom.builder().labels(result.labels()).build();
    }

    private static DnsNameDom resolveNameAt(DnsWireReader reader, int start) {
        NameParseResult result = readName(reader, start, new HashSet<>());
        return DnsNameDom.builder().labels(result.labels()).build();
    }

    private static NameParseResult readName(DnsWireReader reader, int start, Set<Integer> visitedOffsets) {
        int idx = start;
        int endIndex = -1;
        boolean jumped = false;
        int limit = reader.limit();
        List<String> labels = new ArrayList<>();

        while (true) {
            if (idx >= limit) {
                throw new IllegalArgumentException("name exceeds buffer bounds");
            }
            int len = reader.getU8(idx);
            if ((len & 0xC0) == 0xC0) {
                if (idx + 1 >= limit) {
                    throw new IllegalArgumentException("truncated compression pointer");
                }
                int pointer = ((len & 0x3F) << 8) | reader.getU8(idx + 1);
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
            reader.getBytes(idx, labelBytes, 0, len);
            labels.add(new String(labelBytes, StandardCharsets.US_ASCII));
            idx += len;
        }

        if (endIndex < 0) {
            throw new IllegalStateException("invalid name termination");
        }

        return new NameParseResult(labels, endIndex);
    }

    private record NameParseResult(List<String> labels, int endIndex) {
    }
}
