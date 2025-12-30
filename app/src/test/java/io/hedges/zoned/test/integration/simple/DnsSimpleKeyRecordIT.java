// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.test.integration.simple;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.KEYRecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DnsSimpleKeyRecordIT extends DnsSimpleBaseIT {

    @BeforeAll
    static void setUpRecords() throws IOException {
        resetLocalData(List.of(
                "'key.example.test. 300 KEY 257 3 8 AQIDBA=='"));
    }

    @Test
    void resolvesKeyRecordFromUnbound() throws Exception {
        Lookup lookup = new Lookup("key.example.test.", Type.KEY);
        lookup.setResolver(resolver);
        Record[] records = lookup.run();
        assertNotNull(records, "expected DNS KEY records from zoned");

        boolean found = false;
        for (Record record : records) {
            if (record instanceof KEYRecord keyRecord) {
                assertEquals(257, keyRecord.getFlags());
                assertEquals(3, keyRecord.getProtocol());
                assertEquals(8, keyRecord.getAlgorithm());
                assertArrayEquals(new byte[] {1, 2, 3, 4}, keyRecord.getKey());
                found = true;
                break;
            }
        }

        assertTrue(found, "Expected KEY record not found in DNS response");
    }
}
