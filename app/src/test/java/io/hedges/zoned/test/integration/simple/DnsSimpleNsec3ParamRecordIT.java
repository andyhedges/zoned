// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.test.integration.simple;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.NSEC3PARAMRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DnsSimpleNsec3ParamRecordIT extends DnsSimpleBaseIT {

    @BeforeAll
    static void setUpRecords() throws IOException {
        resetLocalData(List.of(
                "'nsec3param.example.test. 300 NSEC3PARAM 1 0 12 AABBCC'"));
    }

    @Test
    void resolvesNsec3ParamRecordFromUnbound() throws Exception {
        Lookup lookup = new Lookup("nsec3param.example.test.", Type.NSEC3PARAM);
        lookup.setResolver(resolver);
        Record[] records = lookup.run();
        assertNotNull(records, "expected DNS NSEC3PARAM records from zoned");

        boolean found = false;
        for (Record record : records) {
            if (record instanceof NSEC3PARAMRecord paramRecord) {
                assertEquals(1, paramRecord.getHashAlgorithm());
                assertEquals(0, paramRecord.getFlags());
                assertEquals(12, paramRecord.getIterations());
                assertArrayEquals(new byte[] {(byte) 0xAA, (byte) 0xBB, (byte) 0xCC}, paramRecord.getSalt());
                found = true;
                break;
            }
        }

        assertTrue(found, "Expected NSEC3PARAM record not found in DNS response");
    }
}
