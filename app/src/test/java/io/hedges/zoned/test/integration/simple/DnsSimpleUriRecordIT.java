// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.test.integration.simple;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;
import org.xbill.DNS.URIRecord;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DnsSimpleUriRecordIT extends DnsSimpleBaseIT {

    @BeforeAll
    static void setUpRecords() throws IOException {
        resetLocalData(List.of(
                "'uri.example.test. 300 URI 10 20 \"https://example.test/service\"'"));
    }

    @Test
    void resolvesUriRecordFromUnbound() throws Exception {
        Lookup lookup = new Lookup("uri.example.test.", Type.URI);
        lookup.setResolver(resolver);
        Record[] records = lookup.run();
        assertNotNull(records, "expected DNS URI records from zoned");

        boolean found = false;
        for (Record record : records) {
            if (record instanceof URIRecord uriRecord) {
                assertEquals(10, uriRecord.getPriority());
                assertEquals(20, uriRecord.getWeight());
                assertEquals("https://example.test/service", uriRecord.getTarget());
                found = true;
                break;
            }
        }

        assertTrue(found, "Expected URI record not found in DNS response");
    }
}
