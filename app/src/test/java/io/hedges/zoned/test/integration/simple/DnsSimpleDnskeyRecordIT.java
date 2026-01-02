// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.test.integration.simple;

import io.hedges.zoned.test.integration.DnsIntegrationBaseIT;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.DNSKEYRecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DnsSimpleDnskeyRecordIT extends DnsIntegrationBaseIT {

    @BeforeAll
    static void setUpRecords() throws IOException {
        resetLocalData(List.of(
                "'dnskey.example.test. 300 DNSKEY 257 3 8 AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=='"));
    }

    @Test
    void resolvesDnskeyRecordFromUnbound() throws Exception {
        Lookup lookup = new Lookup("dnskey.example.test.", Type.DNSKEY);
        lookup.setResolver(resolver);
        Record[] records = lookup.run();
        assertNotNull(records, "expected DNS DNSKEY records from zoned");

        boolean found = false;
        for (Record record : records) {
            if (record instanceof DNSKEYRecord dnskeyRecord) {
                assertEquals(257, dnskeyRecord.getFlags());
                assertEquals(3, dnskeyRecord.getProtocol());
                assertEquals(8, dnskeyRecord.getAlgorithm());
                byte[] expectedKey = new byte[64];
                assertArrayEquals(expectedKey, dnskeyRecord.getKey());
                found = true;
                break;
            }
        }

        assertTrue(found, "Expected DNSKEY record not found in DNS response");
    }
}
