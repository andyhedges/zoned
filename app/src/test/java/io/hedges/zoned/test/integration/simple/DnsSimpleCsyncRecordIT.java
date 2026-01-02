// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.test.integration.simple;

import io.hedges.zoned.test.integration.DnsIntegrationBaseIT;
import io.hedges.zoned.core.dom.DnsRecordTypeDom;
import io.hedges.zoned.core.dom.RDataDom;
import io.hedges.zoned.core.dom.rdata.CsyncRecordDataDom;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;
import org.xbill.DNS.UNKRecord;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DnsSimpleCsyncRecordIT extends DnsIntegrationBaseIT {

    @BeforeAll
    static void setUpRecords() throws IOException {
        resetLocalData(List.of(
                "'csync.example.test. 300 CSYNC 1 0 A NS AAAA'"));
    }

    @Test
    void resolvesCsyncRecordFromUnbound() throws Exception {
        Lookup lookup = new Lookup("csync.example.test.", Type.CSYNC);
        lookup.setResolver(resolver);
        Record[] records = lookup.run();
        assertNotNull(records, "expected DNS CSYNC records from zoned");

        boolean found = false;
        for (Record record : records) {
            if (record.getType() == Type.CSYNC) {
                byte[] rdata = (record instanceof UNKRecord unkRecord)
                        ? unkRecord.getData()
                        : record.rdataToWireCanonical();
                RDataDom dom = CsyncRecordDataDom.from(rdata);
                CsyncRecordDataDom csync = assertInstanceOf(CsyncRecordDataDom.class, dom);
                assertEquals(1L, csync.serial());
                assertEquals(0, csync.flags());
                assertEquals(List.of(DnsRecordTypeDom.A, DnsRecordTypeDom.NS, DnsRecordTypeDom.AAAA), csync.types());
                found = true;
                break;
            }
        }

        assertTrue(found, "Expected CSYNC record not found in DNS response");
    }
}
