// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.test.integration.simple;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.CAARecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DnsSimpleCaaRecordIT extends DnsSimpleBaseIT {

    @BeforeAll
    static void setUpRecords() throws IOException {
        resetLocalData(List.of(
                "'caa.example.test. 300 CAA 0 issue \"letsencrypt.org\"'"));
    }

    @Test
    void resolvesCaaRecordFromUnbound() throws Exception {
        Lookup lookup = new Lookup("caa.example.test.", Type.CAA);
        lookup.setResolver(resolver);
        Record[] records = lookup.run();
        assertNotNull(records, "expected DNS CAA records from zoned");

        boolean found = false;
        for (Record record : records) {
            if (record instanceof CAARecord caaRecord) {
                assertEquals(0, caaRecord.getFlags());
                assertEquals("issue", caaRecord.getTag());
                assertEquals("letsencrypt.org", caaRecord.getValue());
                found = true;
                break;
            }
        }

        assertTrue(found, "Expected CAA record not found in DNS response");
    }
}
