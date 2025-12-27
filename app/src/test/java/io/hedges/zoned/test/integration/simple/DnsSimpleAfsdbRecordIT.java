package io.hedges.zoned.test.integration.simple;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.AFSDBRecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DnsSimpleAfsdbRecordIT extends DnsSimpleBaseIT {

    @BeforeAll
    static void setUpRecords() throws IOException {
        resetLocalData(List.of(
                "\"afsdb.example.test. 300 AFSDB 1 afsdb.example.test.\""));
    }

    @Test
    void resolvesAfsdbRecordFromUnbound() throws Exception {
        Lookup lookup = new Lookup("afsdb.example.test.", Type.AFSDB);
        lookup.setResolver(resolver);
        Record[] records = lookup.run();
        assertNotNull(records, "expected DNS AFSDB records from zoned");

        boolean found = false;
        for (Record record : records) {
            if (record instanceof AFSDBRecord afsdbRecord) {
                assertEquals(1, afsdbRecord.getSubtype());
                assertEquals("afsdb.example.test.", afsdbRecord.getHost().toString());
                found = true;
                break;
            }
        }

        assertTrue(found, "Expected AFSDB record not found in DNS response");
    }
}
