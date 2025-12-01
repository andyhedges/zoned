package io.hedges.zoned.netty;

import io.hedges.zoned.core.DnsServer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.dns.DatagramDnsQueryDecoder;
import io.netty.handler.codec.dns.DatagramDnsResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.net.InetSocketAddress;

public final class UdpNettyDnsServer implements DnsServer {

    private final EventLoopGroup group;
    private final int listenPort;
    private Channel channel;

    public UdpNettyDnsServer(int listenPort) {
        this.listenPort = listenPort;
        this.group = new MultiThreadIoEventLoopGroup(
                Runtime.getRuntime().availableProcessors(),
                NioIoHandler.newFactory()
        );
    }

    public void start() throws InterruptedException {
        // Forwarder backend using the same group
        UdpForwarderBackend forwarder = new UdpForwarderBackend(
                group,
                new InetSocketAddress("1.1.1.1", 53),
                2000
        );
        forwarder.start();


        Bootstrap b = new Bootstrap();
        b.group(group)
         .channel(NioDatagramChannel.class)
         .option(ChannelOption.SO_REUSEADDR, true)
         .handler(new ChannelInitializer<DatagramChannel>() {
             @Override
             protected void initChannel(DatagramChannel ch) {
                 ChannelPipeline p = ch.pipeline();
                 p.addLast("wireLogger", new LoggingHandler(LogLevel.INFO));
                 p.addLast(new DatagramDnsQueryDecoder());
                 p.addLast(new DatagramDnsResponseEncoder());
                 p.addLast(new UdpDnsHandler(/* probable the routing pipeline thing*/));
             }
         });

        this.channel = b.bind(listenPort).sync().channel();
    }

    public void stop() {
        if (channel != null) {
            channel.close();
        }
        group.shutdownGracefully();
    }
}
