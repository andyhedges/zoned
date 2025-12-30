// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.test.integration.simple;

import io.hedges.zoned.core.dom.RDataDom;
import io.hedges.zoned.core.dom.rdata.Eui64RecordDataDom;
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

class DnsSimpleEui64RecordIT extends DnsSimpleBaseIT {

    @BeforeAll
    static void setUpRecords() throws IOException {
        resetLocalData(List.of(
                "'eui64.example.test. 300 EUI64 01-23-45-67-89-ab-cd-ef'"));
    }

    @Test
    void resolvesEui64RecordFromUnbound() throws Exception {
        Lookup lookup = new Lookup("eui64.example.test.", Type.EUI64);
        lookup.setResolver(resolver);
        Record[] records = lookup.run();
        assertNotNull(records, "expected DNS EUI64 records from zoned");

        boolean found = false;
        for (Record record : records) {
            if (record.getType() == Type.EUI64) {
                byte[] rdata = (record instanceof UNKRecord unkRecord)
                        ? unkRecord.getData()
                        : record.rdataToWireCanonical();
                RDataDom dom = Eui64RecordDataDom.from(rdata);
                Eui64RecordDataDom eui64 = assertInstanceOf(Eui64RecordDataDom.class, dom);
                assertArrayEquals(
                        new byte[] {0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xab, (byte) 0xcd, (byte) 0xef},
                        eui64.address()
                );
                found = true;
                break;
            }
        }

        assertTrue(found, "Expected EUI64 record not found in DNS response");
    }
}
