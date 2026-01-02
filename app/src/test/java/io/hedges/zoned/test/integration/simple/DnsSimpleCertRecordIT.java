// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.test.integration.simple;

import io.hedges.zoned.test.integration.DnsIntegrationBaseIT;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.CERTRecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DnsSimpleCertRecordIT extends DnsIntegrationBaseIT {

    @BeforeAll
    static void setUpRecords() throws IOException {
        resetLocalData(List.of(
                "'cert.example.test. 300 CERT 1 12345 8 AAECAwQ='"));
    }

    @Test
    void resolvesCertRecordFromUnbound() throws Exception {
        Lookup lookup = new Lookup("cert.example.test.", Type.CERT);
        lookup.setResolver(resolver);
        Record[] records = lookup.run();
        assertNotNull(records, "expected DNS CERT records from zoned");

        boolean found = false;
        for (Record record : records) {
            if (record instanceof CERTRecord certRecord) {
                assertEquals(1, certRecord.getCertType());
                assertEquals(12345, certRecord.getKeyTag());
                assertEquals(8, certRecord.getAlgorithm());
                byte[] expectedCert = new byte[] {0, 1, 2, 3, 4};
                assertArrayEquals(expectedCert, certRecord.getCert());
                found = true;
                break;
            }
        }

        assertTrue(found, "Expected CERT record not found in DNS response");
    }
}
