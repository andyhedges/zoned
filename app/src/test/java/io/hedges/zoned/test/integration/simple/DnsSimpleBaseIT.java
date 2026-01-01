// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.test.integration.simple;

import io.hedges.zoned.app.ZonedApp;
import io.hedges.zoned.core.DnsServer;
import io.hedges.zoned.test.integration.UnboundContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.xbill.DNS.SimpleResolver;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

abstract class DnsSimpleBaseIT {

    private static DnsServer server;
    private static int appPort;
    private static Path configPath;
    protected SimpleResolver resolver;

    @BeforeAll
    static void startZoned() throws Exception {
        UnboundContainer.getContainer();
        appPort = findFreePort();
        configPath = writeConfig(appPort);
        server = ZonedApp.start(configPath, null).server();
    }

    @BeforeEach
    void setUp() throws Exception {
        resolver = new SimpleResolver(UnboundContainer.UPSTREAM_HOST);
        resolver.setPort(appPort);
    }

    @AfterAll
    static void tearDown() throws IOException {
        if (server != null) {
            server.stop();
        }
        if (configPath != null) {
            Files.deleteIfExists(configPath);
        }
    }

    protected static void resetLocalData(List<String> records) throws IOException {
        UnboundContainer.clearLocalData();
        UnboundContainer.addLocalData(records);
    }

    private static int findFreePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private static Path writeConfig(int serverPort) throws IOException {
        String yaml = String.join(
                "\n",
                "serverPort: " + serverPort,
                "activeDnsServerPool: default",
                "dnsServerPools:",
                "  default:",
                "    - do53:",
                "        host: " + UnboundContainer.UPSTREAM_HOST,
                "        port: " + UnboundContainer.UPSTREAM_PORT,
                "");
        Path path = Files.createTempFile("zoned-", ".yaml");
        Files.writeString(path, yaml);
        return path;
    }
}
