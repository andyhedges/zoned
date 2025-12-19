package io.hedges.zoned.netty;

import io.hedges.zoned.core.DnsRequestHandler;
import io.hedges.zoned.core.DnsServer;
import io.hedges.zoned.core.DnsServerStartException;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.Setter;


public final class UdpNettyDnsServer implements DnsServer {

    private final EventLoopGroup group;
    private final int listenPort;
    private Channel channel;
    @Setter
    private DnsRequestHandler requestHandler;

    public UdpNettyDnsServer(EventLoopGroup group, int listenPort) {
        this.listenPort = listenPort;
        this.group = group;
    }

    public void start() throws DnsServerStartException {
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioDatagramChannel.class)
             .option(ChannelOption.SO_REUSEADDR, true)
             .handler(new ChannelInitializer<DatagramChannel>() {
                 @Override
                 protected void initChannel(DatagramChannel ch) {
                     ChannelPipeline p = ch.pipeline();
                     p.addLast("wireLogger", new LoggingHandler(LogLevel.INFO));
                     p.addLast(new DnsDatagramDecoder());
                     p.addLast(new DnsDatagramEncoder());
                     p.addLast(new UdpDnsHandler(requestHandler));
                 }
             });

            this.channel = b.bind(listenPort).sync().channel();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DnsServerStartException("DNS server start interrupted", e);
        } catch (Exception e) {
            throw new DnsServerStartException("DNS server failed to start", e);
        }
    }

    public void stop() {
        if (channel != null) {
            channel.close();
        }
        group.shutdownGracefully();
    }
}
