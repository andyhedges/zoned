package io.hedges.zoned.test.integration.simple;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.HTTPSRecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DnsSimpleHttpsRecordIT extends DnsSimpleBaseIT {

    @BeforeAll
    static void setUpRecords() throws IOException {
        resetLocalData(List.of(
                "\"https.example.test. 300 HTTPS 1 svc.example.test.\""));
    }

    @Test
    void resolvesHttpsRecordFromUnbound() throws Exception {
        Lookup lookup = new Lookup("https.example.test.", Type.HTTPS);
        lookup.setResolver(resolver);
        Record[] records = lookup.run();
        assertNotNull(records, "expected DNS HTTPS records from zoned");

        boolean found = false;
        for (Record record : records) {
            if (record instanceof HTTPSRecord httpsRecord) {
                assertEquals(1, httpsRecord.getSvcPriority());
                assertEquals("svc.example.test.", httpsRecord.getTargetName().toString());
                assertTrue(httpsRecord.getSvcParamKeys().isEmpty());
                found = true;
                break;
            }
        }

        assertTrue(found, "Expected HTTPS record not found in DNS response");
    }
}
