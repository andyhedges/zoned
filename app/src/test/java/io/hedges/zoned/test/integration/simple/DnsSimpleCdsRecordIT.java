// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.test.integration.simple;

import io.hedges.zoned.test.integration.DnsIntegrationBaseIT;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.CDSRecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DnsSimpleCdsRecordIT extends DnsIntegrationBaseIT {

    @BeforeAll
    static void setUpRecords() throws IOException {
        resetLocalData(List.of(
                "'cds.example.test. 300 CDS 12345 8 1 01020304'"));
    }

    @Test
    void resolvesCdsRecordFromUnbound() throws Exception {
        Lookup lookup = new Lookup("cds.example.test.", Type.CDS);
        lookup.setResolver(resolver);
        Record[] records = lookup.run();
        assertNotNull(records, "expected DNS CDS records from zoned");

        boolean found = false;
        for (Record record : records) {
            if (record instanceof CDSRecord cdsRecord) {
                assertEquals(12345, cdsRecord.getFootprint());
                assertEquals(8, cdsRecord.getAlgorithm());
                assertEquals(1, cdsRecord.getDigestID());
                assertArrayEquals(new byte[] {1, 2, 3, 4}, cdsRecord.getDigest());
                found = true;
                break;
            }
        }

        assertTrue(found, "Expected CDS record not found in DNS response");
    }
}
