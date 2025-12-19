package io.hedges.zoned.app;

import io.hedges.zoned.core.DnsImplementationProvider;
import io.hedges.zoned.core.DnsServer;
import io.hedges.zoned.netty.NettyDnsImplementationProvider;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

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
        ZonedConfig config = loadConfig(resolvedConfigPath);
        if (serverPortOverride != null) {
            validatePort(serverPortOverride, "--port");
            config.setServerPort(serverPortOverride);
        }
        validateConfig(config, resolvedConfigPath);

        DnsServerConfig serverConfig = resolveDnsServer(config);
        TransportSelection selection = selectTransport(serverConfig, resolvedConfigPath);
        if (selection.kind() == TransportKind.TCP) {
            throw new IllegalArgumentException("TCP transport is not implemented yet");
        }
        if (selection.kind() == TransportKind.DOH) {
            throw new IllegalArgumentException("DoH transport is not implemented yet");
        }
        if (selection.kind() == TransportKind.DOQ) {
            throw new IllegalArgumentException("DoQ transport is not implemented yet");
        }
        InetSocketAddress upstream = selection.address();
        DnsImplementationProvider dip = new NettyDnsImplementationProvider(config.getServerPort(), upstream);
        DnsServer server = dip.server();
        server.requestHandler(new DefaultDnsRequestRouter(dip.client())).start();
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        System.out.println("zoned DNS server listening on UDP " + config.getServerPort());
        return 0;
    }

    private static ZonedConfig loadConfig(Path configPath) throws IOException {
        Path normalized = configPath.toAbsolutePath().normalize();
        if (!Files.exists(normalized)) {
            throw new IllegalArgumentException("Config file not found: " + normalized);
        }
        try (InputStream input = Files.newInputStream(normalized)) {
            Yaml yaml = new Yaml(new Constructor(ZonedConfig.class));
            ZonedConfig config = yaml.load(input);
            return config == null ? new ZonedConfig() : config;
        }
    }

    private static void validateConfig(ZonedConfig config, Path configPath) {
        if (config.getServerPort() <= 0 || config.getServerPort() > 65535) {
            throw new IllegalArgumentException("serverPort must be between 1 and 65535 in " + configPath);
        }
        if (config.getDnsServerPools() == null || config.getDnsServerPools().isEmpty()) {
            throw new IllegalArgumentException("dnsServerPools must be set in " + configPath);
        }
        String activeSet = config.getActiveDnsServerPool();
        if (activeSet == null || activeSet.isBlank()) {
            throw new IllegalArgumentException("activeDnsServerPool must be set in " + configPath);
        }
        List<DnsServerConfig> active = config.getDnsServerPools().get(activeSet);
        if (active == null || active.isEmpty()) {
            throw new IllegalArgumentException("activeDnsServerPool not found or empty: " + activeSet);
        }
        for (DnsServerConfig server : active) {
            selectTransport(server, configPath);
        }
    }

    private static DnsServerConfig resolveDnsServer(ZonedConfig config) {
        Map<String, List<DnsServerConfig>> pools = config.getDnsServerPools();
        List<DnsServerConfig> active = pools.get(config.getActiveDnsServerPool());
        return active.get(0);
    }

    private static TransportSelection selectTransport(DnsServerConfig server, Path configPath) {
        boolean hasUdp = server.getUdp() != null;
        boolean hasTcp = server.getTcp() != null;
        boolean hasDoh = server.getDoh() != null;
        boolean hasDoq = server.getDoq() != null;
        boolean hasDo53 = server.getDo53() != null;

        int setCount = 0;
        if (hasUdp) {
            setCount++;
        }
        if (hasTcp) {
            setCount++;
        }
        if (hasDoh) {
            setCount++;
        }
        if (hasDoq) {
            setCount++;
        }
        if (hasDo53) {
            setCount++;
        }
        if (setCount == 0) {
            throw new IllegalArgumentException("dns server transport must be set in " + configPath);
        }
        if (setCount > 1) {
            throw new IllegalArgumentException("dns server transport must be exclusive in " + configPath);
        }

        if (hasDo53) {
            Do53TransportConfig do53 = server.getDo53();
            String host = do53.getHost();
            if (host == null || host.isBlank()) {
                throw new IllegalArgumentException("do53 host must be set in " + configPath);
            }
            validatePort(do53.getPort(), "do53 port");
            return new TransportSelection(TransportKind.DO53, new InetSocketAddress(host, do53.getPort()), null, null);
        }
        if (hasUdp) {
            UdpTransportConfig udp = server.getUdp();
            String host = udp.getHost();
            if (host == null || host.isBlank()) {
                throw new IllegalArgumentException("udp host must be set in " + configPath);
            }
            validatePort(udp.getPort(), "udp port");
            if (hasTcp) {
                TcpTransportConfig tcp = server.getTcp();
                String tcpHost = tcp.getHost();
                if (tcpHost == null || tcpHost.isBlank()) {
                    throw new IllegalArgumentException("tcp host must be set in " + configPath);
                }
                validatePort(tcp.getPort(), "tcp port");
            }
            return new TransportSelection(TransportKind.UDP, new InetSocketAddress(host, udp.getPort()), null, null);
        }
        if (hasTcp) {
            TcpTransportConfig tcp = server.getTcp();
            String host = tcp.getHost();
            if (host == null || host.isBlank()) {
                throw new IllegalArgumentException("tcp host must be set in " + configPath);
            }
            validatePort(tcp.getPort(), "tcp port");
            return new TransportSelection(TransportKind.TCP, new InetSocketAddress(host, tcp.getPort()), null, null);
        }
        if (server.getDoh() != null) {
            String url = server.getDoh().getUrl();
            if (url == null || url.isBlank()) {
                throw new IllegalArgumentException("doh url must be set in " + configPath);
            }
            return new TransportSelection(TransportKind.DOH, null, url, null);
        }
        DoqTransportConfig doq = server.getDoq();
        String host = doq.getHost();
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("doq host must be set in " + configPath);
        }
        validatePort(doq.getPort(), "doq port");
        return new TransportSelection(TransportKind.DOQ, null, null, doq);
    }

    private enum TransportKind {
        DO53,
        UDP,
        TCP,
        DOH,
        DOQ
    }

    private record TransportSelection(
            TransportKind kind,
            InetSocketAddress address,
            String url,
            DoqTransportConfig doqConfig) {
    }

    private static void validatePort(int port, String source) {
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException(source + " must be between 1 and 65535");
        }
    }
}
