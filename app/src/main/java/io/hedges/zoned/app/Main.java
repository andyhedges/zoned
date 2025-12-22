package io.hedges.zoned.app;

import io.hedges.zoned.core.DnsServer;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

@Command(name = "zoned", mixinStandardHelpOptions = true, description = "Zoned DNS server")
public final class Main implements Callable<Integer> {
    @Option(names = "--config", paramLabel = "PATH", description = "Path to config file")
    private String configPath;

    @Option(names = "--port", paramLabel = "PORT", description = "Override server port")
    private Integer serverPortOverride;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        Path resolvedConfigPath = ConfigPathResolver.resolve(configPath);
        ZonedApp.StartedServer started = ZonedApp.start(resolvedConfigPath, serverPortOverride);
        DnsServer server = started.server();
        CountDownLatch shutdown = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop();
            shutdown.countDown();
        }));
        System.out.println("zoned DNS server listening on UDP " + started.port());
        shutdown.await();
        return 0;
    }
}
