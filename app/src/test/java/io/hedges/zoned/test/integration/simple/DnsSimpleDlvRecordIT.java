// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.test.integration.simple;

import io.hedges.zoned.test.integration.DnsIntegrationBaseIT;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.DLVRecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DnsSimpleDlvRecordIT extends DnsIntegrationBaseIT {

    @BeforeAll
    static void setUpRecords() throws IOException {
        resetLocalData(List.of(
                "'dlv.example.test. 300 DLV 12345 8 1 01020304'"));
    }

    @Test
    void resolvesDlvRecordFromUnbound() throws Exception {
        Lookup lookup = new Lookup("dlv.example.test.", Type.DLV);
        lookup.setResolver(resolver);
        Record[] records = lookup.run();
        assertNotNull(records, "expected DNS DLV records from zoned");

        boolean found = false;
        for (Record record : records) {
            if (record instanceof DLVRecord dlvRecord) {
                assertEquals(12345, dlvRecord.getFootprint());
                assertEquals(8, dlvRecord.getAlgorithm());
                assertEquals(1, dlvRecord.getDigestID());
                assertArrayEquals(new byte[] {1, 2, 3, 4}, dlvRecord.getDigest());
                found = true;
                break;
            }
        }

        assertTrue(found, "Expected DLV record not found in DNS response");
    }
}
