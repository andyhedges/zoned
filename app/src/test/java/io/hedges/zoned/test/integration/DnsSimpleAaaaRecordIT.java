package io.hedges.zoned.test.integration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DnsSimpleAaaaRecordIT extends DnsSimpleBaseIT {

    @BeforeAll
    static void setUpRecords() throws IOException {
        resetLocalData(List.of(
                "\"aaaa.example.test. 300 AAAA 2001:db8::1\""));
    }

    @Test
    void resolvesAaaaRecordFromUnbound() throws Exception {
        Lookup lookup = new Lookup("aaaa.example.test.", Type.AAAA);
        lookup.setResolver(resolver);
        Record[] records = lookup.run();
        assertNotNull(records, "expected DNS AAAA records from zoned");

        boolean found = false;
        for (Record record : records) {
            if (record instanceof AAAARecord aaaaRecord) {
                if ("2001:db8:0:0:0:0:0:1".equals(aaaaRecord.getAddress().getHostAddress())) {
                    found = true;
                    break;
                }
            }
        }

        assertTrue(found, "Expected AAAA address not found in DNS response");
    }
}
