package net.hedges.dns;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioDatagramChannel;

import io.netty.handler.codec.dns.DatagramDnsQueryDecoder;
import io.netty.handler.codec.dns.DatagramDnsResponseEncoder;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class Main {

    private final EventLoopGroup group;
    private final Channel channel;

    private Main(EventLoopGroup group, Channel channel) {
        this.group = group;
        this.channel = channel;
    }

    public static Main start(DnsServerConfig config, Zone zone) throws InterruptedException {

        EventLoopGroup group = new MultiThreadIoEventLoopGroup(
                config.workerThreads(),
                NioIoHandler.newFactory()
        );

        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_REUSEADDR, config.reuseAddress())
                .localAddress(new InetSocketAddress(config.port()))
                .handler(new ChannelInitializer<NioDatagramChannel>() {
                    @Override
                    protected void initChannel(NioDatagramChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new DatagramDnsQueryDecoder());
                        p.addLast(new DatagramDnsResponseEncoder());
                        p.addLast(new RobustDnsHandler(zone));
                    }
                });

        Channel ch = b.bind().sync().channel();
        System.out.println("DNS server listening on UDP port "
                + ((InetSocketAddress) (b.config().localAddress())).getPort());
        return new Main(group, ch);
    }

    public void awaitClose() throws InterruptedException {
        channel.closeFuture().sync();
    }

    public void stop() {
        channel.close();
        group.shutdownGracefully(0, 5, TimeUnit.SECONDS);
    }

    public static void main(String[] args) throws Exception {
        Zone zone = Zone.example();  // your in memory zone model
        DnsServerConfig config = DnsServerConfig.defaultConfig();
        Main server = start(config, zone);

        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));

        server.awaitClose();
    }
}