package io.hedges.zoned.test.integration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DnsSimpleARecordIT extends DnsSimpleBaseIT {

    @BeforeAll
    static void setUpRecords() throws IOException {
        resetLocalData(List.of(
                "\"example.test. 60 A 192.0.2.123\""));
    }

    @Test
    void resolvesARecordFromUnbound() throws Exception {
        Lookup lookup = new Lookup("example.test.", Type.A);
        lookup.setResolver(resolver);
        Record[] records = lookup.run();
        assertNotNull(records, "expected DNS A records from zoned");

        boolean found = false;
        for (Record record : records) {
            if (record instanceof ARecord aRecord) {
                if ("192.0.2.123".equals(aRecord.getAddress().getHostAddress())) {
                    found = true;
                    break;
                }
            }
        }

        assertTrue(found, "Expected address not found in DNS response");
    }
}
