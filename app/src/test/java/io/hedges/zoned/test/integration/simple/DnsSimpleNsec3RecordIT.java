// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.test.integration.simple;

import io.hedges.zoned.test.integration.DnsIntegrationBaseIT;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.NSEC3Record;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DnsSimpleNsec3RecordIT extends DnsIntegrationBaseIT {

    @BeforeAll
    static void setUpRecords() throws IOException {
        resetLocalData(List.of(
                "'nsec3.example.test. 300 NSEC3 1 0 12 AABBCC A1B2C3D4 A AAAA'"));
    }

    @Test
    void resolvesNsec3RecordFromUnbound() throws Exception {
        Lookup lookup = new Lookup("nsec3.example.test.", Type.NSEC3);
        lookup.setResolver(resolver);
        Record[] records = lookup.run();
        assertNotNull(records, "expected DNS NSEC3 records from zoned");

        boolean found = false;
        for (Record record : records) {
            if (record instanceof NSEC3Record nsec3Record) {
                assertEquals(1, nsec3Record.getHashAlgorithm());
                assertEquals(0, nsec3Record.getFlags());
                assertEquals(12, nsec3Record.getIterations());
                assertArrayEquals(new byte[] {(byte) 0xAA, (byte) 0xBB, (byte) 0xCC}, nsec3Record.getSalt());
                assertTrue(nsec3Record.hasType(Type.A));
                assertTrue(nsec3Record.hasType(Type.AAAA));
                found = true;
                break;
            }
        }

        assertTrue(found, "Expected NSEC3 record not found in DNS response");
    }
}
