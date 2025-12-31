// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.test.integration.simple;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.RRSIGRecord;
import org.xbill.DNS.Record;
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

class DnsSimpleRrsigRecordIT extends DnsSimpleBaseIT {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @BeforeAll
    static void setUpRecords() throws IOException {
        resetLocalData(List.of(
                "'signer.example.test. 3600 IN A 192.0.2.1'",
                "'signer.example.test. 3600 IN RRSIG A 1 3 3600 20260101000000 20240101000000 12345 signer.example.test. AQIDBA=='",
                "'signer.example.test. 3600 IN DNSKEY 257 3 8 AQIDBA=='"));
    }

    @Test
    /*
     * RRSIGRecord.getRRsetType() returns the covered type (A), and RRset.getType()
     * uses that.
     * Lookup/Cache.addMessage() compares the RRset type to the query type. So for a
     * Type.RRSIG query, the answer RRset’s type is treated as A, and Lookup
     * concludes “no
     * matching type” → NXRRSET, even though the wire response contains an RRSIG.
     * 
     * Thus we Use SimpleResolver + Message and inspect the ANSWER section directly.
     * 
     */
    void resolvesRrsigRecordFromUnbound() throws Exception {

        Name name = Name.fromString("signer.example.test.");
        Record question = Record.newRecord(name, Type.RRSIG, DClass.IN);
        Message query = Message.newQuery(question);

        Message response = resolver.send(query);
        List<Record> records = response.getSection(Section.ANSWER);
        assertNotNull(records, "expected DNS RRSIG records from zoned");

        boolean found = false;
        for (Record record : records) {
            if (record instanceof RRSIGRecord rrsigRecord) {
                assertEquals(Type.A, rrsigRecord.getTypeCovered());
                assertEquals(1, rrsigRecord.getAlgorithm());
                assertEquals(3, rrsigRecord.getLabels());
                assertEquals(3600L, rrsigRecord.getOrigTTL());
                assertEquals(12345, rrsigRecord.getFootprint());
                assertEquals("signer.example.test.", rrsigRecord.getSigner().toString());
                assertArrayEquals(new byte[] { 1, 2, 3, 4 }, rrsigRecord.getSignature());
                Instant expire = LocalDateTime.parse("20260101000000", TIME_FORMAT).toInstant(ZoneOffset.UTC);
                Instant inception = LocalDateTime.parse("20240101000000", TIME_FORMAT).toInstant(ZoneOffset.UTC);
                assertEquals(expire.getEpochSecond(), rrsigRecord.getExpire().getEpochSecond());
                assertEquals(inception.getEpochSecond(), rrsigRecord.getTimeSigned().getEpochSecond());
                found = true;
                break;
            }
        }

        assertTrue(found, "Expected RRSIG record not found in DNS response");
    }
}
