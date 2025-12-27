package io.hedges.zoned.test.integration.simple;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SOARecord;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DnsSimpleSoaRecordIT extends DnsSimpleBaseIT {

    @BeforeAll
    static void setUpRecords() throws IOException {
        resetLocalData(List.of(
                "\"example.test. 300 SOA ns.example.test. hostmaster.example.test. 1 7200 3600 1209600 300\""));
    }

    @Test
    void resolvesSoaRecordFromUnbound() throws Exception {
        Lookup lookup = new Lookup("example.test.", Type.SOA);
        lookup.setResolver(resolver);
        Record[] records = lookup.run();
        assertNotNull(records, "expected DNS SOA records from zoned");

        boolean found = false;
        for (Record record : records) {
            if (record instanceof SOARecord soaRecord) {
                assertEquals("ns.example.test.", soaRecord.getHost().toString());
                assertEquals("hostmaster.example.test.", soaRecord.getAdmin().toString());
                assertEquals(1L, soaRecord.getSerial());
                assertEquals(7200L, soaRecord.getRefresh());
                assertEquals(3600L, soaRecord.getRetry());
                assertEquals(1209600L, soaRecord.getExpire());
                assertEquals(300L, soaRecord.getMinimum());
                found = true;
                break;
            }
        }

        assertTrue(found, "Expected SOA record not found in DNS response");
    }
}
