package net.hedges.dns;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.dns.DatagramDnsQueryDecoder;
import io.netty.handler.codec.dns.DatagramDnsResponseEncoder;
import net.hedges.dns.rule.RuleEngine;

public final class UdpDnsServer {
    private final EventLoopGroup group = new MultiThreadIoEventLoopGroup(
            Runtime.getRuntime().availableProcessors(),
            NioIoHandler.newFactory()
    );
    private final RuleEngine ruleEngine;

    public UdpDnsServer(RuleEngine ruleEngine) {
        this.ruleEngine = ruleEngine;
    }

    public void start(int port) throws InterruptedException {
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_REUSEADDR, true)
                .handler(new ChannelInitializer<DatagramChannel>() {
                    @Override
                    protected void initChannel(DatagramChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new DatagramDnsQueryDecoder());
                        p.addLast(new DatagramDnsResponseEncoder());
                        p.addLast(new UdpDnsHandler(ruleEngine));
                    }
                });

        b.bind(port).sync();
    }

    public void shutdown() {
        group.shutdownGracefully();
    }
}
