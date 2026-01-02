// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.test.integration.simple;

import io.hedges.zoned.test.integration.DnsIntegrationBaseIT;
import io.hedges.zoned.core.dom.RDataDom;
import io.hedges.zoned.core.dom.rdata.Eui48RecordDataDom;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;
import org.xbill.DNS.UNKRecord;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DnsSimpleEui48RecordIT extends DnsIntegrationBaseIT {

    @BeforeAll
    static void setUpRecords() throws IOException {
        resetLocalData(List.of(
                "'eui48.example.test. 300 EUI48 01-23-45-67-89-ab'"));
    }

    @Test
    void resolvesEui48RecordFromUnbound() throws Exception {
        Lookup lookup = new Lookup("eui48.example.test.", Type.EUI48);
        lookup.setResolver(resolver);
        Record[] records = lookup.run();
        assertNotNull(records, "expected DNS EUI48 records from zoned");

        boolean found = false;
        for (Record record : records) {
            if (record.getType() == Type.EUI48) {
                byte[] rdata = (record instanceof UNKRecord unkRecord)
                        ? unkRecord.getData()
                        : record.rdataToWireCanonical();
                RDataDom dom = Eui48RecordDataDom.from(rdata);
                Eui48RecordDataDom eui48 = assertInstanceOf(Eui48RecordDataDom.class, dom);
                assertArrayEquals(new byte[] {0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xab}, eui48.address());
                found = true;
                break;
            }
        }

        assertTrue(found, "Expected EUI48 record not found in DNS response");
    }
}
