package io.hedges.zoned.test.integration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SVCBRecord;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DnsSimpleSvcbRecordIT extends DnsSimpleBaseIT {

    @BeforeAll
    static void setUpRecords() throws IOException {
        resetLocalData(List.of(
                "\"svcb.example.test. 300 SVCB 1 svc.example.test.\""));
    }

    @Test
    void resolvesSvcbRecordFromUnbound() throws Exception {
        Lookup lookup = new Lookup("svcb.example.test.", Type.SVCB);
        lookup.setResolver(resolver);
        Record[] records = lookup.run();
        assertNotNull(records, "expected DNS SVCB records from zoned");

        boolean found = false;
        for (Record record : records) {
            if (record instanceof SVCBRecord svcbRecord) {
                assertEquals(1, svcbRecord.getSvcPriority());
                assertEquals("svc.example.test.", svcbRecord.getTargetName().toString());
                assertTrue(svcbRecord.getSvcParamKeys().isEmpty());
                found = true;
                break;
            }
        }

        assertTrue(found, "Expected SVCB record not found in DNS response");
    }
}
