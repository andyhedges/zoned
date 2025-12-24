package io.hedges.zoned.test.integration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.CNAMERecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DnsSanityCnameRecordIT extends DnsSanityBaseIT {

    @BeforeAll
    static void setUpRecords() throws IOException {
        resetLocalData(List.of(
                "\"cname.example.test. 300 CNAME example.test.\""));
    }

    @Test
    void resolvesCnameRecordFromUnbound() throws Exception {
        Lookup lookup = new Lookup("cname.example.test.", Type.CNAME);
        lookup.setResolver(resolver);
        Record[] records = lookup.run();
        assertNotNull(records, "expected DNS CNAME records from zoned");

        boolean found = false;
        for (Record record : records) {
            if (record instanceof CNAMERecord cRecord) {
                if ("example.test.".equals(cRecord.getTarget().toString())) {
                    found = true;
                    break;
                }
            }
        }

        assertTrue(found, "Expected address not found in DNS response");
    }
}
