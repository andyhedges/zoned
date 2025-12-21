package io.hedges.zoned.test.integration;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.HealthCheck;

import io.hedges.zoned.app.ZonedApp;
import io.hedges.zoned.core.DnsServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import org.xbill.DNS.ARecord;
import org.xbill.DNS.CNAMERecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TXTRecord;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class DnsSanityIT {

    private static final int UPSTREAM_PORT = 9457;
    private static final String UPSTREAM_HOST = "127.0.0.1";

    @Container
    static final GenericContainer<?> compose = new GenericContainer<>("alpinelinux/unbound:latest")
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("unbound/unbound.conf"),
                    "/etc/unbound/unbound.conf")
            .withCreateContainerCmdModifier(cmd -> {
                ExposedPort udp53 = ExposedPort.udp(53);
                ExposedPort tcp53 = ExposedPort.tcp(53);
                cmd.withExposedPorts(tcp53, udp53);
                Ports ports = new Ports();
                ports.bind(udp53, Ports.Binding.bindPort(UPSTREAM_PORT));
                ports.bind(tcp53, Ports.Binding.bindPort(UPSTREAM_PORT));
                cmd.getHostConfig().withPortBindings(ports);
            })
            .withCreateContainerCmdModifier(cmd ->
                cmd.withHealthcheck(new HealthCheck()
                    .withTest(List.of("CMD-SHELL", "unbound-control status >/dev/null 2>&1"))
                    .withInterval(10_000_000_000L) // 10s (nanoseconds)
                    .withTimeout(3_000_000_000L) // 3s
                    .withRetries(5)))
            .waitingFor(Wait.forHealthcheck());

    private static DnsServer server;
    private static int appPort;
    private static Path configPath;
    private SimpleResolver resolver;

    @BeforeAll
    static void startZoned() throws Exception {
        appPort = findFreePort();
        configPath = writeConfig(appPort);
        server = ZonedApp.start(configPath, null).server();
    }

    @BeforeEach
    void setUp() throws Exception {
        resolver = new SimpleResolver(UPSTREAM_HOST);
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

    @Test
    void resolvesARecordFromUnbound() throws Exception {
        Lookup lookup = new Lookup("example.test.", Type.A);
        lookup.setResolver(resolver);
        Record[] records = lookup.run();
        assertNotNull(records, "expected DNS records from zoned");

        boolean found = false;
        for (Record record : records) {
            if (record instanceof ARecord aRecord) {
                if ("192.0.2.123".equals(aRecord.getAddress().getHostAddress())) {
                    found = true;
                    break;
                }
            }
        }

        assertTrue(found, "Expected address not found in DNS response");
    }

    @Test
    void resolvesCnameRecordFromUnbound() throws Exception {
        Lookup lookup = new Lookup("cname.example.test.", Type.CNAME);
        lookup.setResolver(resolver);
        Record[] records = lookup.run();
        assertNotNull(records, "expected DNS records from zoned");

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

    @Test
    void resolvesTxtRecordFromUnbound() throws Exception {
        Lookup lookup = new Lookup("txt.example.test.", Type.TXT);
        lookup.setResolver(resolver);
        Record[] records = lookup.run();

        assertNotNull(records, "'expected DNS records, lookup failed: " + lookup.getErrorString() + "'");

        List<byte[]> expected = List.of(
                "Hello, World!".getBytes(StandardCharsets.US_ASCII),
                "Goodbye".getBytes(StandardCharsets.US_ASCII));

        assertEquals(1, records.length);
        assertInstanceOf(TXTRecord.class, records[0]);

        TXTRecord txtRecord = (TXTRecord) records[0];
        List<byte[]> actual = txtRecord.getStringsAsByteArrays();

        assertEquals(expected.size(), actual.size(),
                "Number of strings in TXT record differ from expected: " + txtRecord.getStrings());

        for (int i = 0; i < expected.size(); i++) {
            assertArrayEquals(
                    expected.get(i),
                    actual.get(i),
                    "Mismatch in TXT character-string at index " + i);
        }
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
                "        host: " + UPSTREAM_HOST,
                "        port: " + UPSTREAM_PORT,
                "");
        Path path = Files.createTempFile("zoned-", ".yaml");
        Files.writeString(path, yaml);
        return path;
    }
}
