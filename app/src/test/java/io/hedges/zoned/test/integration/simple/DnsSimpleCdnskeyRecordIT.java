// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.test.integration.simple;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.CDNSKEYRecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DnsSimpleCdnskeyRecordIT extends DnsSimpleBaseIT {

    @BeforeAll
    static void setUpRecords() throws IOException {
        resetLocalData(List.of(
                "'cdnskey.example.test. 300 CDNSKEY 257 3 8 AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=='"));
    }

    @Test
    void resolvesCdnskeyRecordFromUnbound() throws Exception {
        Lookup lookup = new Lookup("cdnskey.example.test.", Type.CDNSKEY);
        lookup.setResolver(resolver);
        Record[] records = lookup.run();
        assertNotNull(records, "expected DNS CDNSKEY records from zoned");

        boolean found = false;
        for (Record record : records) {
            if (record instanceof CDNSKEYRecord cdnskeyRecord) {
                assertEquals(257, cdnskeyRecord.getFlags());
                assertEquals(3, cdnskeyRecord.getProtocol());
                assertEquals(8, cdnskeyRecord.getAlgorithm());
                byte[] expectedKey = new byte[64];
                assertArrayEquals(expectedKey, cdnskeyRecord.getKey());
                found = true;
                break;
            }
        }

        assertTrue(found, "Expected CDNSKEY record not found in DNS response");
    }
}
