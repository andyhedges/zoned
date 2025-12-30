// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.test.integration.simple;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.DNAMERecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DnsSimpleDnameRecordIT extends DnsSimpleBaseIT {

    @BeforeAll
    static void setUpRecords() throws IOException {
        resetLocalData(List.of(
                "'dname.example.test. 300 DNAME target.example.test.'"));
    }

    @Test
    void resolvesDnameRecordFromUnbound() throws Exception {
        Lookup lookup = new Lookup("dname.example.test.", Type.DNAME);
        lookup.setResolver(resolver);
        Record[] records = lookup.run();
        assertNotNull(records, "expected DNS DNAME records from zoned");

        boolean found = false;
        for (Record record : records) {
            if (record instanceof DNAMERecord dnameRecord) {
                assertEquals("target.example.test.", dnameRecord.getTarget().toString());
                found = true;
                break;
            }
        }

        assertTrue(found, "Expected DNAME record not found in DNS response");
    }
}
