// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.test.integration.simple;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DnsSimpleSrvRecordIT extends DnsSimpleBaseIT {

    @BeforeAll
    static void setUpRecords() throws IOException {
        resetLocalData(List.of(
                "'_sip._tcp.example.test. 300 SRV 10 20 5060 target.example.test.'"));
    }

    @Test
    void resolvesSrvRecordFromUnbound() throws Exception {
        Lookup lookup = new Lookup("_sip._tcp.example.test.", Type.SRV);
        lookup.setResolver(resolver);
        Record[] records = lookup.run();
        assertNotNull(records, "expected DNS SRV records from zoned");

        boolean found = false;
        for (Record record : records) {
            if (record instanceof SRVRecord srvRecord) {
                assertEquals(10, srvRecord.getPriority());
                assertEquals(20, srvRecord.getWeight());
                assertEquals(5060, srvRecord.getPort());
                assertEquals("target.example.test.", srvRecord.getTarget().toString());
                found = true;
                break;
            }
        }

        assertTrue(found, "Expected SRV record not found in DNS response");
    }
}
