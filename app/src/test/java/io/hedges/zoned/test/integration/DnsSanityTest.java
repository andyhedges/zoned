package io.hedges.zoned.test.integration;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.Ports.Binding;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.resolver.dns.DnsNameResolver;
import io.netty.resolver.dns.DnsNameResolverBuilder;
import io.netty.resolver.dns.SingletonDnsServerAddressStreamProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class DnsSanityTest {

    private static final int PORT = 9457;

    @Container
    static final GenericContainer<?> compose =
            new GenericContainer<>("mvance/unbound:latest")
                    .withCopyFileToContainer(
                            MountableFile.forClasspathResource("unbound/unbound.conf"),
                            "/opt/unbound/etc/unbound/unbound.conf"
                    )
                    .withCreateContainerCmdModifier(cmd -> {
                        ExposedPort udp53 = ExposedPort.udp(53);
                        ExposedPort tcp53 = ExposedPort.tcp(53);

                        cmd.withExposedPorts(tcp53, udp53);

                        Ports ports = new Ports();
                        ports.bind(udp53, Ports.Binding.bindPort(PORT));
                        ports.bind(tcp53, Ports.Binding.bindPort(PORT));
                        cmd.getHostConfig().withPortBindings(ports);
                    })
                    .waitingFor(Wait.forHealthcheck());

    private EventLoopGroup group;
    private DnsNameResolver resolver;


    @BeforeEach
    void setUp() {
        group = new NioEventLoopGroup(1);
        EventLoop loop = (EventLoop) group.next();

        InetSocketAddress dnsServer =
                new InetSocketAddress("127.0.0.1", PORT);

        resolver = new DnsNameResolverBuilder(loop)
                .datagramChannelType(NioDatagramChannel.class)
                .nameServerProvider(
                        new SingletonDnsServerAddressStreamProvider(dnsServer)
                )
                .searchDomains(List.of())     // important
                .recursionDesired(true)
                .build();
    }

    @AfterEach
    void tearDown() {
        if (resolver != null) {
            resolver.close();
        }
        if (group != null) {
            group.shutdownGracefully();
        }
    }

    @Test
    void resolvesARecordFromUnbound() throws Exception {
        var fut = resolver.resolveAll("example.test.");
        List<InetAddress> addrs = fut.get();

        assertFalse(addrs.isEmpty());

        addrs.forEach(System.out::println);

        boolean found = addrs.stream()
                             .anyMatch(a -> "192.0.2.123".equals(a.getHostAddress()));

        assertTrue(found, addrs.toString());
    }
}
