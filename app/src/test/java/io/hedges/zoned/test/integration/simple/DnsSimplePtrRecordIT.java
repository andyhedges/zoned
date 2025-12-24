package io.hedges.zoned.test.integration.simple;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.PTRRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DnsSimplePtrRecordIT extends DnsSimpleBaseIT {

    @BeforeAll
    static void setUpRecords() throws IOException {
        resetLocalData(List.of(
                "\"1.0.0.127.in-addr.arpa. 300 PTR ptr.example.test.\""));
    }

    @Test
    void resolvesPtrRecordFromUnbound() throws Exception {
        Lookup lookup = new Lookup("1.0.0.127.in-addr.arpa.", Type.PTR);
        lookup.setResolver(resolver);
        Record[] records = lookup.run();
        assertNotNull(records, "expected DNS PTR records from zoned");

        boolean found = false;
        for (Record record : records) {
            if (record instanceof PTRRecord ptrRecord) {
                assertEquals("ptr.example.test.", ptrRecord.getTarget().toString());
                found = true;
                break;
            }
        }

        assertTrue(found, "Expected PTR record not found in DNS response");
    }
}
