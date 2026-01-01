// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.test.integration.simple;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SMIMEARecord;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DnsSimpleSmimeaRecordIT extends DnsSimpleBaseIT {

    @BeforeAll
    static void setUpRecords() throws IOException {
        resetLocalData(List.of(
                "'smimea.example.test. 300 SMIMEA 1 2 1 0102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f20'"));
    }

    @Test
    void resolvesSmimeaRecordFromUnbound() throws Exception {
        Lookup lookup = new Lookup("smimea.example.test.", Type.SMIMEA);
        lookup.setResolver(resolver);
        Record[] records = lookup.run();
        assertNotNull(records, "expected DNS SMIMEA records from zoned");

        boolean found = false;
        for (Record record : records) {
            if (record instanceof SMIMEARecord smimeaRecord) {
                assertEquals(1, smimeaRecord.getCertificateUsage());
                assertEquals(2, smimeaRecord.getSelector());
                assertEquals(1, smimeaRecord.getMatchingType());
                assertArrayEquals(sequence(32), smimeaRecord.getCertificateAssociationData());
                found = true;
                break;
            }
        }

        assertTrue(found, "Expected SMIMEA record not found in DNS response");
    }

    private static byte[] sequence(int length) {
        byte[] out = new byte[length];
        for (int i = 0; i < length; i++) {
            out[i] = (byte) (i + 1);
        }
        return out;
    }
}
