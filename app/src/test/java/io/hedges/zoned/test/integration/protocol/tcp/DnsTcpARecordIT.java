// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.test.integration.protocol.tcp;

import io.hedges.zoned.test.integration.DnsIntegrationBaseIT;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DnsTcpARecordIT extends DnsIntegrationBaseIT {

    @BeforeAll
    static void setUpRecords() throws IOException {
        resetLocalData(List.of(
                "\"tcp-a.example.test. 300 A 192.0.2.10\""));
    }

    @Test
    void resolvesARecordOverTcp() throws Exception {
        resolver.setTCP(true);

        Lookup lookup = new Lookup("tcp-a.example.test.", Type.A);
        lookup.setResolver(resolver);
        Record[] records = lookup.run();
        assertNotNull(records, "expected DNS A records from zoned");

        boolean found = false;
        for (Record record : records) {
            if (record instanceof ARecord aRecord) {
                assertEquals(InetAddress.getByName("192.0.2.10"), aRecord.getAddress());
                found = true;
                break;
            }
        }

        assertTrue(found, "Expected A record not found in DNS response");
    }
}
