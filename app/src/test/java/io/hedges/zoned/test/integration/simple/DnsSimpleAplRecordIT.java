// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.test.integration.simple;

import io.hedges.zoned.test.integration.DnsIntegrationBaseIT;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.APLRecord;
import org.xbill.DNS.Address;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DnsSimpleAplRecordIT extends DnsIntegrationBaseIT {

    @BeforeAll
    static void setUpRecords() throws IOException {
        resetLocalData(List.of(
                "\"apl.example.test. 300 APL 1:192.0.2.0/24 !2:2001:db8::/32\""));
    }

    @Test
    void resolvesAplRecordFromUnbound() throws Exception {
        Lookup lookup = new Lookup("apl.example.test.", Type.APL);
        lookup.setResolver(resolver);
        Record[] records = lookup.run();
        assertNotNull(records, "expected DNS APL records from zoned");

        boolean found = false;
        for (Record record : records) {
            if (record instanceof APLRecord aplRecord) {
                List<APLRecord.Element> elements = aplRecord.getElements();
                assertEquals(2, elements.size());

                boolean ipv4Found = false;
                boolean ipv6Found = false;
                for (APLRecord.Element element : elements) {
                    if (element.family == Address.IPv4) {
                        assertEquals(24, element.prefixLength);
                        assertTrue(!element.negative);
                        InetAddress ipv4Address = (InetAddress) element.address;
                        assertEquals("192.0.2.0", ipv4Address.getHostAddress());
                        ipv4Found = true;
                    } else if (element.family == Address.IPv6) {
                        assertEquals(32, element.prefixLength);
                        assertTrue(element.negative);
                        InetAddress ipv6Address = (InetAddress) element.address;
                        assertEquals("2001:db8:0:0:0:0:0:0", ipv6Address.getHostAddress());
                        ipv6Found = true;
                    }
                }
                assertTrue(ipv4Found, "Expected IPv4 APL element not found");
                assertTrue(ipv6Found, "Expected IPv6 APL element not found");
                found = true;
                break;
            }
        }

        assertTrue(found, "Expected APL record not found in DNS response");
    }
}
