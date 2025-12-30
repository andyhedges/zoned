// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.test.integration.simple;

import io.hedges.zoned.core.dom.RDataDom;
import io.hedges.zoned.core.dom.rdata.DhcidRecordDataDom;
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

class DnsSimpleDhcidRecordIT extends DnsSimpleBaseIT {

    @BeforeAll
    static void setUpRecords() throws IOException {
        resetLocalData(List.of(
                "'dhcid.example.test. 300 DHCID AQIDBA=='"));
    }

    @Test
    void resolvesDhcidRecordFromUnbound() throws Exception {
        Lookup lookup = new Lookup("dhcid.example.test.", Type.DHCID);
        lookup.setResolver(resolver);
        Record[] records = lookup.run();
        assertNotNull(records, "expected DNS DHCID records from zoned");

        boolean found = false;
        for (Record record : records) {
            if (record.getType() == Type.DHCID) {
                byte[] rdata = (record instanceof UNKRecord unkRecord)
                        ? unkRecord.getData()
                        : record.rdataToWireCanonical();
                RDataDom dom = DhcidRecordDataDom.from(rdata);
                DhcidRecordDataDom dhcid = assertInstanceOf(DhcidRecordDataDom.class, dom);
                assertArrayEquals(new byte[] {1, 2, 3, 4}, dhcid.identifier());
                found = true;
                break;
            }
        }

        assertTrue(found, "Expected DHCID record not found in DNS response");
    }
}
