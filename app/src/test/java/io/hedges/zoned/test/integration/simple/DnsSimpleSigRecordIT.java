// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.test.integration.simple;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.SIGRecord;
import org.xbill.DNS.Section;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DnsSimpleSigRecordIT extends DnsSimpleBaseIT {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @BeforeAll
    static void setUpRecords() throws IOException {
        resetLocalData(List.of(
                "'sig.example.test. 300 A 192.0.2.1'",
                "'sig.example.test. 300 SIG A 1 3 3600 20250101000000 20240101000000 12345 signer.example.test. AQIDBA=='"));
    }

    @Test
    void resolvesSigRecordFromUnbound() throws Exception {
        Name name = Name.fromString("sig.example.test.");
        Record question = Record.newRecord(name, Type.SIG, DClass.IN);
        Message query = Message.newQuery(question);

        Message response = resolver.send(query);
        List<Record> records = response.getSection(Section.ANSWER);
        assertNotNull(records, "expected DNS SIG records from zoned");

        boolean found = false;
        for (Record record : records) {
            if (record instanceof SIGRecord sigRecord) {
                assertEquals(Type.A, sigRecord.getTypeCovered());
                assertEquals(1, sigRecord.getAlgorithm());
                assertEquals(3, sigRecord.getLabels());
                assertEquals(3600L, sigRecord.getOrigTTL());
                assertEquals(12345, sigRecord.getFootprint());
                assertEquals("signer.example.test.", sigRecord.getSigner().toString());
                assertArrayEquals(new byte[] {1, 2, 3, 4}, sigRecord.getSignature());
                Instant expire = LocalDateTime.parse("20250101000000", TIME_FORMAT).toInstant(ZoneOffset.UTC);
                Instant inception = LocalDateTime.parse("20240101000000", TIME_FORMAT).toInstant(ZoneOffset.UTC);
                assertEquals(expire.getEpochSecond(), sigRecord.getExpire().getEpochSecond());
                assertEquals(inception.getEpochSecond(), sigRecord.getTimeSigned().getEpochSecond());
                found = true;
                break;
            }
        }

        assertTrue(found, "Expected SIG record not found in DNS response");
    }
}
