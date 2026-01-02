// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.test.integration.simple;

import io.hedges.zoned.test.integration.DnsIntegrationBaseIT;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;


class DnsSimpleTkeyRecordIT extends DnsIntegrationBaseIT {

    @BeforeAll
    static void setUpRecords() throws IOException {
        resetLocalData(List.of(
                "'tkey.example.test. 300 TYPE249 \\# 40 03616c67076578616d706c650474657374000000000100000002000300000004010203040002aabb'"));
    }

    @Test
    @Disabled("Unbound does not serve TKEY from local-data (meta type)")
    void resolvesTkeyRecordFromUnbound() throws Exception {
        throw new InterruptedException("Disabled test");
    }
}
