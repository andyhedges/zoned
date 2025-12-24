package io.hedges.zoned.test.integration.simple;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.TXTRecord;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DnsSimpleTxtRecordIT extends DnsSimpleBaseIT {

    @BeforeAll
    static void setUpRecords() throws IOException {
        resetLocalData(List.of(
                "'txt.example.test. 300 TXT \"Hello, World!\"\"Goodbye\"'"));
    }

    @Test
    void resolvesTxtRecordFromUnbound() throws Exception {
        Lookup lookup = new Lookup("txt.example.test.", Type.TXT);
        lookup.setResolver(resolver);
        Record[] records = lookup.run();

        assertNotNull(records, "'expected DNS records, lookup failed: " + lookup.getErrorString() + "'");

        List<byte[]> expected = List.of(
                "Hello, World!".getBytes(StandardCharsets.US_ASCII),
                "Goodbye".getBytes(StandardCharsets.US_ASCII));

        assertEquals(1, records.length);
        assertInstanceOf(TXTRecord.class, records[0]);

        TXTRecord txtRecord = (TXTRecord) records[0];
        List<byte[]> actual = txtRecord.getStringsAsByteArrays();

        assertEquals(expected.size(), actual.size(),
                "Number of strings in TXT record differ from expected: " + txtRecord.getStrings());

        for (int i = 0; i < expected.size(); i++) {
            assertArrayEquals(
                    expected.get(i),
                    actual.get(i),
                    "Mismatch in TXT character-string at index " + i);
        }
    }
}
