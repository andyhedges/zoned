// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.test.integration.simple;

import io.hedges.zoned.test.integration.DnsIntegrationBaseIT;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.NAPTRRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DnsSimpleNaptrRecordIT extends DnsIntegrationBaseIT {

    @BeforeAll
    static void setUpRecords() throws IOException {
        resetLocalData(List.of(
                "'naptr.example.test. 300 NAPTR 100 200 \"U\" \"E2U+sip\" \"!^.*$!sip:info@example.com!\" example.test.'"));
    }

    @Test
    void resolvesNaptrRecordFromUnbound() throws Exception {
        Lookup lookup = new Lookup("naptr.example.test.", Type.NAPTR);
        lookup.setResolver(resolver);
        Record[] records = lookup.run();
        assertNotNull(records, "expected DNS NAPTR records from zoned");

        boolean found = false;
        for (Record record : records) {
            if (record instanceof NAPTRRecord naptrRecord) {
                assertEquals(100, naptrRecord.getOrder());
                assertEquals(200, naptrRecord.getPreference());
                assertEquals("U", naptrRecord.getFlags());
                assertEquals("E2U+sip", naptrRecord.getService());
                assertEquals("!^.*$!sip:info@example.com!", naptrRecord.getRegexp());
                assertEquals("example.test.", naptrRecord.getReplacement().toString());
                found = true;
                break;
            }
        }

        assertTrue(found, "Expected NAPTR record not found in DNS response");
    }
}
