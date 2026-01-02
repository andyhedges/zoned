// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.test.integration.truncation;

import io.hedges.zoned.test.integration.DnsIntegrationBaseIT;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.DClass;
import org.xbill.DNS.EDNSOption;
import org.xbill.DNS.Flags;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DnsTruncationLargeResponseIT extends DnsIntegrationBaseIT {

    private static final String TEST_NAME = "big.example.test.";
    private static final int RECORD_COUNT = 40;
    private static final int TXT_PAYLOAD_LENGTH = 220;

    @BeforeAll
    static void setUpRecords() throws IOException {
        resetLocalData(buildLargeTxtRecords());
    }

    @Test
    void marksLargeUdpResponsesAsTruncated() throws Exception {
        resolver.setEDNS(0, 512, 0, new EDNSOption[0]);
        resolver.setIgnoreTruncation(true);

        Name name = Name.fromString(TEST_NAME);
        Record question = Record.newRecord(name, Type.TXT, DClass.IN);
        Message query = Message.newQuery(question);
        Message response = resolver.send(query);

        assertNotNull(response, "expected DNS response from zoned");
        assertTrue(response.getHeader().getFlag(Flags.TC), "expected truncation flag for large UDP response");
    }

    private static List<String> buildLargeTxtRecords() {
        List<String> records = new ArrayList<>();
        for (int i = 0; i < RECORD_COUNT; i++) {
            String payload = psuedoRandomString(TXT_PAYLOAD_LENGTH);
            records.add("'" + TEST_NAME + " 300 TXT \"" + payload + "\"'");
        }
        return records;
    }

    private static Random rnd = new Random(1337);

    private static String psuedoRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for(int i = 0; i < length; i++){
            sb.append((char)('a' + rnd.nextInt(26)));
        }
        return sb.toString();
    }
}
