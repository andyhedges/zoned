package io.hedges.zoned.test.integration;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HealthCheck;
import com.github.dockerjava.api.model.Ports;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

final class UnboundContainer {

    static final int UPSTREAM_PORT = 9457;
    static final String UPSTREAM_HOST = "127.0.0.1";

    private static final String LOCAL_DATA_FILE = "local-data.conf";
    private static final String CONTAINER_LOCAL_DATA_PATH = "/etc/unbound/" + LOCAL_DATA_FILE;
    private static final String UNBOUND_CONF_RESOURCE = "unbound/unbound.conf";
    private static final String LOCAL_DATA_RESOURCE = "unbound/local-data.conf";
    private static final int FILE_MODE_0644 = 0644;
    private static final GenericContainer<?> CONTAINER = createContainer();
    private static volatile boolean started;

    private UnboundContainer() {
    }

    static GenericContainer<?> getContainer() {
        ensureStarted();
        return CONTAINER;
    }

    static synchronized void addLocalData(List<String> records) throws IOException {
        ensureStarted();
        Path file = writeLocalDataFile(records);
        CONTAINER.copyFileToContainer(
                MountableFile.forHostPath(file, FILE_MODE_0644),
                CONTAINER_LOCAL_DATA_PATH);
        reload();
    }

    static synchronized void clearLocalData() throws IOException {
        ensureStarted();
        Path file = writeLocalDataFile(List.of());
        CONTAINER.copyFileToContainer(
                MountableFile.forHostPath(file, FILE_MODE_0644),
                CONTAINER_LOCAL_DATA_PATH);
        reload();
    }

    private static void ensureStarted() {
        if (started) {
            return;
        }
        synchronized (UnboundContainer.class) {
            if (!started) {
                CONTAINER.start();
                started = true;
                Runtime.getRuntime().addShutdownHook(new Thread(CONTAINER::stop));
            }
        }
    }

    private static GenericContainer<?> createContainer() {
        return new GenericContainer<>("alpinelinux/unbound:latest")
                .withCopyFileToContainer(
                        MountableFile.forClasspathResource(UNBOUND_CONF_RESOURCE, FILE_MODE_0644),
                        "/etc/unbound/unbound.conf")
                .withCopyFileToContainer(
                        MountableFile.forClasspathResource(LOCAL_DATA_RESOURCE, FILE_MODE_0644),
                        CONTAINER_LOCAL_DATA_PATH)
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
    }

    private static void reload() throws IOException {
        try {
            var result = CONTAINER.execInContainer("unbound-control", "reload");
            if (result.getExitCode() != 0) {
                throw new IOException("unbound-control reload failed: " + result.getStderr());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while reloading unbound", e);
        }
    }

    private static Path writeLocalDataFile(List<String> records) throws IOException {
        Path file = Files.createTempFile("unbound-local-data-", ".conf");
        StringBuilder contents = new StringBuilder();
        contents.append("server:\n");
        for (String record : records) {
            contents.append("  local-data: ")
                    .append(record)
                    .append('\n');
        }
        Files.writeString(file, contents.toString(), StandardCharsets.UTF_8);
        System.out.println("Unbound local-data.conf contents:\n" + contents);
        return file;
    }
}
