// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.test.integration.simple;

import io.hedges.zoned.test.integration.DnsIntegrationBaseIT;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.LOCRecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DnsSimpleLocRecordIT extends DnsIntegrationBaseIT {

    @BeforeAll
    static void setUpRecords() throws IOException {
        resetLocalData(List.of(
                "'loc.example.test. 300 LOC 0 0 0.000 N 0 0 0.000 E 0m 1m 1m 1m'"));
    }

    @Test
    void resolvesLocRecordFromUnbound() throws Exception {
        Lookup lookup = new Lookup("loc.example.test.", Type.LOC);
        lookup.setResolver(resolver);
        Record[] records = lookup.run();
        assertNotNull(records, "expected DNS LOC records from zoned");

        boolean found = false;
        for (Record record : records) {
            if (record instanceof LOCRecord locRecord) {
                assertEquals(0.0, locRecord.getLatitude(), 0.0001);
                assertEquals(0.0, locRecord.getLongitude(), 0.0001);
                assertEquals(0.0, locRecord.getAltitude(), 0.0001);
                assertEquals(1.0, locRecord.getSize(), 0.0001);
                assertEquals(1.0, locRecord.getHPrecision(), 0.0001);
                assertEquals(1.0, locRecord.getVPrecision(), 0.0001);
                found = true;
                break;
            }
        }

        assertTrue(found, "Expected LOC record not found in DNS response");
    }
}
