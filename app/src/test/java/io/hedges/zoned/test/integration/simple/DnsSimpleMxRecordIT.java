package io.hedges.zoned.test.integration.simple;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.MXRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DnsSimpleMxRecordIT extends DnsSimpleBaseIT {

    @BeforeAll
    static void setUpRecords() throws IOException {
        resetLocalData(List.of(
                "\"mx.example.test. 300 MX 10 mail.example.test.\""));
    }

    @Test
    void resolvesMxRecordFromUnbound() throws Exception {
        Lookup lookup = new Lookup("mx.example.test.", Type.MX);
        lookup.setResolver(resolver);
        Record[] records = lookup.run();
        assertNotNull(records, "expected DNS MX records from zoned");

        boolean found = false;
        for (Record record : records) {
            if (record instanceof MXRecord mxRecord) {
                assertEquals(10, mxRecord.getPriority());
                assertEquals("mail.example.test.", mxRecord.getTarget().toString());
                found = true;
                break;
            }
        }

        assertTrue(found, "Expected MX record not found in DNS response");
    }
}
