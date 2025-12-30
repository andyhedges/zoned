// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.test.integration.simple;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.OPENPGPKEYRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DnsSimpleOpenPgpKeyRecordIT extends DnsSimpleBaseIT {

    @BeforeAll
    static void setUpRecords() throws IOException {
        resetLocalData(List.of(
                "'openpgpkey.example.test. 300 OPENPGPKEY AQIDBA=='"));
    }

    @Test
    void resolvesOpenPgpKeyRecordFromUnbound() throws Exception {
        Lookup lookup = new Lookup("openpgpkey.example.test.", Type.OPENPGPKEY);
        lookup.setResolver(resolver);
        Record[] records = lookup.run();
        assertNotNull(records, "expected DNS OPENPGPKEY records from zoned");

        boolean found = false;
        for (Record record : records) {
            if (record instanceof OPENPGPKEYRecord openpgpkeyRecord) {
                assertArrayEquals(new byte[] {1, 2, 3, 4}, openpgpkeyRecord.getCert());
                found = true;
                break;
            }
        }

        assertTrue(found, "Expected OPENPGPKEY record not found in DNS response");
    }
}
