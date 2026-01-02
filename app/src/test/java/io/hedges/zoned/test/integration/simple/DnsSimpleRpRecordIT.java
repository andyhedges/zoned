// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.test.integration.simple;

import io.hedges.zoned.test.integration.DnsIntegrationBaseIT;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.RPRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DnsSimpleRpRecordIT extends DnsIntegrationBaseIT {

    @BeforeAll
    static void setUpRecords() throws IOException {
        resetLocalData(List.of(
                "'rp.example.test. 300 RP mbox.example.test. text.example.test.'"));
    }

    @Test
    void resolvesRpRecordFromUnbound() throws Exception {
        Lookup lookup = new Lookup("rp.example.test.", Type.RP);
        lookup.setResolver(resolver);
        Record[] records = lookup.run();
        assertNotNull(records, "expected DNS RP records from zoned");

        boolean found = false;
        for (Record record : records) {
            if (record instanceof RPRecord rpRecord) {
                assertEquals("mbox.example.test.", rpRecord.getMailbox().toString());
                assertEquals("text.example.test.", rpRecord.getTextDomain().toString());
                found = true;
                break;
            }
        }

        assertTrue(found, "Expected RP record not found in DNS response");
    }
}
