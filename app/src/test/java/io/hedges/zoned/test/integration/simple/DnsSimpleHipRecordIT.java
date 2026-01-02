// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.test.integration.simple;

import io.hedges.zoned.test.integration.DnsIntegrationBaseIT;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.HIPRecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DnsSimpleHipRecordIT extends DnsIntegrationBaseIT {

    @BeforeAll
    static void setUpRecords() throws IOException {
        // TODO: Unbound does not support optional rendezvous server names for HIP records.
        resetLocalData(List.of(
                "'hip.example.test. 300 HIP 1 0102030405060708090A0B0C0D0E0F10 AQIDBA=='"));
    }

    @Test
    void resolvesHipRecordFromUnbound() throws Exception {
        Lookup lookup = new Lookup("hip.example.test.", Type.HIP);
        lookup.setResolver(resolver);
        Record[] records = lookup.run();
        assertNotNull(records, "expected DNS HIP records from zoned");

        boolean found = false;
        for (Record record : records) {
            if (record instanceof HIPRecord hipRecord) {
                assertArrayEquals(
                        new byte[] {
                                0x01, 0x02, 0x03, 0x04,
                                0x05, 0x06, 0x07, 0x08,
                                0x09, 0x0A, 0x0B, 0x0C,
                                0x0D, 0x0E, 0x0F, 0x10
                        },
                        hipRecord.getHit()
                );
                assertEquals(1, hipRecord.getAlgorithm());
                assertArrayEquals(new byte[] {1, 2, 3, 4}, hipRecord.getKey());
                assertEquals(0, hipRecord.getRvServers().size());
                found = true;
                break;
            }
        }

        assertTrue(found, "Expected HIP record not found in DNS response");
    }
}
