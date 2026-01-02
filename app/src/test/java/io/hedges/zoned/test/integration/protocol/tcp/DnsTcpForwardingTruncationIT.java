// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.test.integration.protocol.tcp;

import io.hedges.zoned.test.integration.DnsIntegrationBaseIT;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.DClass;
import org.xbill.DNS.EDNSOption;
import org.xbill.DNS.Flags;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Section;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DnsTcpForwardingTruncationIT extends DnsIntegrationBaseIT {

    private static final String TEST_NAME = "tcp-large.example.test.";
    private static final int RECORD_COUNT = 40;
    private static final int TXT_PAYLOAD_LENGTH = 220;

    @BeforeAll
    static void setUpRecords() throws IOException {
        resetLocalData(buildLargeTxtRecords());
    }

    @Test
    void resolvesLargeResponseOverTcpAfterUdpTruncation() throws Exception {
        resolver.setEDNS(0, 512, 0, new EDNSOption[0]);
        resolver.setIgnoreTruncation(true);

        Message udpResponse = sendQuery(false);
        assertNotNull(udpResponse, "expected UDP response from zoned");
        assertTrue(udpResponse.getHeader().getFlag(Flags.TC), "expected truncation flag for UDP response");

        Message tcpResponse = sendQuery(true);
        assertNotNull(tcpResponse, "expected TCP response from zoned");
        assertFalse(tcpResponse.getHeader().getFlag(Flags.TC), "expected full response over TCP");
        assertEquals(RECORD_COUNT, tcpResponse.getSectionArray(Section.ANSWER).length,
                "expected full answer set over TCP");
    }

    private Message sendQuery(boolean useTcp) throws Exception {
        resolver.setTCP(useTcp);
        Name name = Name.fromString(TEST_NAME);
        Record question = Record.newRecord(name, Type.TXT, DClass.IN);
        Message query = Message.newQuery(question);
        return resolver.send(query);
    }

    private static List<String> buildLargeTxtRecords() {
        List<String> records = new ArrayList<>();
        for (int i = 0; i < RECORD_COUNT; i++) {
            String payload = "x".repeat(TXT_PAYLOAD_LENGTH - 37) + "-" + UUID.randomUUID();
            records.add("'" + TEST_NAME + " 300 TXT \"" + payload + "\"'");
        }
        return records;
    }
}
