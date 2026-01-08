// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core;

import io.hedges.zoned.core.dom.DnsHeaderDom;
import io.hedges.zoned.core.dom.DnsMessageDom;
import io.hedges.zoned.core.dom.DnsNameDom;
import io.hedges.zoned.core.dom.DnsQuestionDom;
import io.hedges.zoned.core.dom.DnsResourceRecordDom;
import io.hedges.zoned.core.dom.rdata.OptRecordDataDom;

import java.util.List;

public final class DnsMessageEncoder {

    private DnsMessageEncoder() {
    }

    public static void encode(DnsMessageDom message, DnsWireWriter writer) {
        if (message == null) {
            throw new IllegalArgumentException("message is null");
        }
        if (message.header() == null) {
            throw new IllegalArgumentException("message.header is null");
        }
        if (writer == null) {
            throw new IllegalArgumentException("writer is null");
        }

        List<DnsQuestionDom> questions = safeQuestions(message.questions());
        List<DnsResourceRecordDom> answers = safeRecords(message.answers());
        List<DnsResourceRecordDom> authorities = safeRecords(message.authorities());
        List<DnsResourceRecordDom> additionals = safeRecords(message.additionals());

        writer.writeU16(message.header().id() & 0xFFFF);
        writer.writeU16(encodeFlags(message.header()));
        writer.writeU16(questions.size());
        writer.writeU16(answers.size());
        writer.writeU16(authorities.size());
        writer.writeU16(additionals.size());

        for (DnsQuestionDom question : questions) {
            writeName(writer, question.name());
            writer.writeU16(question.recordType().code());
            writer.writeU16(question.recordClass().code());
        }

        for (DnsResourceRecordDom record : answers) {
            writeResourceRecord(writer, record);
        }

        for (DnsResourceRecordDom record : authorities) {
            writeResourceRecord(writer, record);
        }

        for (DnsResourceRecordDom record : additionals) {
            writeResourceRecord(writer, record);
        }
    }

    private static List<DnsQuestionDom> safeQuestions(List<DnsQuestionDom> questions) {
        return questions == null ? List.of() : questions;
    }

    private static List<DnsResourceRecordDom> safeRecords(List<DnsResourceRecordDom> records) {
        return records == null ? List.of() : records;
    }

    private static void writeResourceRecord(DnsWireWriter writer, DnsResourceRecordDom record) {
        writeName(writer, record.name());
        writer.writeU16(record.type().code());
        if (record.rdata() instanceof OptRecordDataDom opt) {
            writer.writeU16(opt.udpPayloadSize());
            long ttl = ((opt.extendedRCode() & 0xFFL) << 24)
                    | ((opt.version() & 0xFFL) << 16)
                    | (opt.dnssecOk() ? 0x8000L : 0L);
            writer.writeU32(ttl);
        } else {
            writer.writeU16(record.recordClass().code());
            writer.writeU32(record.ttlSeconds());
        }
        byte[] rdata = record.rdata() == null ? new byte[0] : record.rdata().to();
        writer.writeU16(rdata.length);
        writer.writeBytes(rdata);
    }

    private static void writeName(DnsWireWriter writer, DnsNameDom name) {
        if (name == null || name.labels() == null) {
            throw new IllegalArgumentException("name is null");
        }
        int totalLength = 1;
        for (byte[] label : name.labels()) {
            if (label == null) {
                throw new IllegalArgumentException("label is null");
            }
            if (label.length > 63) {
                throw new IllegalArgumentException("label exceeds 63 bytes");
            }
            totalLength += 1 + label.length;
            if (totalLength > 255) {
                throw new IllegalArgumentException("name exceeds 255 bytes");
            }
            writer.writeU8(label.length);
            writer.writeBytes(label);
        }
        writer.writeU8(0);
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
