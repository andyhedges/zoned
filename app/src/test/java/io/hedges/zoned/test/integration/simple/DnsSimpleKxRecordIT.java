// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.test.integration.simple;

import io.hedges.zoned.test.integration.DnsIntegrationBaseIT;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.KXRecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DnsSimpleKxRecordIT extends DnsIntegrationBaseIT {

    @BeforeAll
    static void setUpRecords() throws IOException {
        resetLocalData(List.of(
                "'kx.example.test. 300 KX 25 kx-target.example.test.'"));
    }

    @Test
    void resolvesKxRecordFromUnbound() throws Exception {
        Lookup lookup = new Lookup("kx.example.test.", Type.KX);
        lookup.setResolver(resolver);
        Record[] records = lookup.run();
        assertNotNull(records, "expected DNS KX records from zoned");

        boolean found = false;
        for (Record record : records) {
            if (record instanceof KXRecord kxRecord) {
                assertEquals(25, kxRecord.getPreference());
                assertEquals("kx-target.example.test.", kxRecord.getTarget().toString());
                found = true;
                break;
            }
        }

        assertTrue(found, "Expected KX record not found in DNS response");
    }
}
