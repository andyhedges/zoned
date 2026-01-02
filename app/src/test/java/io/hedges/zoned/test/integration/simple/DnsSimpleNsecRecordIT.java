// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.test.integration.simple;

import io.hedges.zoned.test.integration.DnsIntegrationBaseIT;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.NSECRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DnsSimpleNsecRecordIT extends DnsIntegrationBaseIT {

    @BeforeAll
    static void setUpRecords() throws IOException {
        resetLocalData(List.of(
                "'nsec.example.test. 300 NSEC next.example.test. A AAAA'"));
    }

    @Test
    void resolvesNsecRecordFromUnbound() throws Exception {
        Lookup lookup = new Lookup("nsec.example.test.", Type.NSEC);
        lookup.setResolver(resolver);
        Record[] records = lookup.run();
        assertNotNull(records, "expected DNS NSEC records from zoned");

        boolean found = false;
        for (Record record : records) {
            if (record instanceof NSECRecord nsecRecord) {
                assertEquals("next.example.test.", nsecRecord.getNext().toString());
                assertTrue(nsecRecord.hasType(Type.A));
                assertTrue(nsecRecord.hasType(Type.AAAA));
                found = true;
                break;
            }
        }

        assertTrue(found, "Expected NSEC record not found in DNS response");
    }
}
