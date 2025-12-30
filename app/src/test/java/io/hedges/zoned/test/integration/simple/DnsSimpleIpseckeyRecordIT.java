// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.test.integration.simple;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.IPSECKEYRecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DnsSimpleIpseckeyRecordIT extends DnsSimpleBaseIT {

    @BeforeAll
    static void setUpRecords() throws IOException {
        resetLocalData(List.of(
                "'ipseckey.example.test. 300 IPSECKEY 10 1 1 192.0.2.1 AQIDBA=='"));
    }

    @Test
    void resolvesIpseckeyRecordFromUnbound() throws Exception {
        Lookup lookup = new Lookup("ipseckey.example.test.", Type.IPSECKEY);
        lookup.setResolver(resolver);
        Record[] records = lookup.run();
        assertNotNull(records, "expected DNS IPSECKEY records from zoned");

        boolean found = false;
        for (Record record : records) {
            if (record instanceof IPSECKEYRecord ipseckeyRecord) {
                assertEquals(10, ipseckeyRecord.getPrecedence());
                assertEquals(1, ipseckeyRecord.getGatewayType());
                assertEquals(1, ipseckeyRecord.getAlgorithmType());
                InetAddress gateway = assertInstanceOf(InetAddress.class, ipseckeyRecord.getGateway());
                assertEquals("192.0.2.1", gateway.getHostAddress());
                assertArrayEquals(new byte[] {1, 2, 3, 4}, ipseckeyRecord.getKey());
                found = true;
                break;
            }
        }

        assertTrue(found, "Expected IPSECKEY record not found in DNS response");
    }
}
