// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.test.integration.simple;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SSHFPRecord;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DnsSimpleSshfpRecordIT extends DnsSimpleBaseIT {

    @BeforeAll
    static void setUpRecords() throws IOException {
        resetLocalData(List.of(
                "'sshfp.example.test. 300 SSHFP 1 1 00112233445566778899aabbccddeeff00112233'"));
    }

    @Test
    void resolvesSshfpRecordFromUnbound() throws Exception {
        Lookup lookup = new Lookup("sshfp.example.test.", Type.SSHFP);
        lookup.setResolver(resolver);
        Record[] records = lookup.run();
        assertNotNull(records, "expected DNS SSHFP records from zoned");

        boolean found = false;
        for (Record record : records) {
            if (record instanceof SSHFPRecord sshfpRecord) {
                assertEquals(1, sshfpRecord.getAlgorithm());
                assertEquals(1, sshfpRecord.getDigestType());
                assertArrayEquals(fingerprintBytes(), sshfpRecord.getFingerPrint());
                found = true;
                break;
            }
        }

        assertTrue(found, "Expected SSHFP record not found in DNS response");
    }

    private static byte[] fingerprintBytes() {
        return new byte[] {
                0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88, (byte) 0x99,
                (byte) 0xaa, (byte) 0xbb, (byte) 0xcc, (byte) 0xdd, (byte) 0xee, (byte) 0xff,
                0x00, 0x11, 0x22, 0x33
        };
    }
}
