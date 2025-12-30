// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.test.integration.simple;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.DSRecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DnsSimpleDsRecordIT extends DnsSimpleBaseIT {

    @BeforeAll
    static void setUpRecords() throws IOException {
        resetLocalData(List.of(
                "'ds.example.test. 300 DS 12345 8 1 01020304'"));
    }

    @Test
    void resolvesDsRecordFromUnbound() throws Exception {
        Lookup lookup = new Lookup("ds.example.test.", Type.DS);
        lookup.setResolver(resolver);
        Record[] records = lookup.run();
        assertNotNull(records, "expected DNS DS records from zoned");

        boolean found = false;
        for (Record record : records) {
            if (record instanceof DSRecord dsRecord) {
                assertEquals(12345, dsRecord.getFootprint());
                assertEquals(8, dsRecord.getAlgorithm());
                assertEquals(1, dsRecord.getDigestID());
                assertArrayEquals(new byte[] {1, 2, 3, 4}, dsRecord.getDigest());
                found = true;
                break;
            }
        }

        assertTrue(found, "Expected DS record not found in DNS response");
    }
}
