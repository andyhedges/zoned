package net.hedges.dns;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import net.hedges.dns.rule.DnsRule;
import net.hedges.dns.rule.ForwardRule;
import net.hedges.dns.rule.RuleEngine;

import java.net.InetSocketAddress;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new MultiThreadIoEventLoopGroup(
                Runtime.getRuntime().availableProcessors(),
                NioIoHandler.newFactory()
        );

        UdpForwarderBackend forwarder = new UdpForwarderBackend(
                group,
                new InetSocketAddress("8.8.8.8", 53),
                2000
        );
        forwarder.start();

        List<DnsRule> rules = List.of(new ForwardRule(forwarder));

        RuleEngine engine = new RuleEngine(rules);

        UdpDnsServer server = new UdpDnsServer(engine);
        server.start(53);

    }
}
