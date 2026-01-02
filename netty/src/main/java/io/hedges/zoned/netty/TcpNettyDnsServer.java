// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.netty;

import io.hedges.zoned.core.DnsRequestHandler;
import io.hedges.zoned.core.DnsServer;
import io.hedges.zoned.core.DnsServerStartException;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.Setter;

public final class TcpNettyDnsServer implements DnsServer {

    private final EventLoopGroup group;
    private final int listenPort;
    private final boolean manageGroupLifecycle;
    private Channel channel;
    @Setter
    private DnsRequestHandler requestHandler;

    public TcpNettyDnsServer(EventLoopGroup group, int listenPort) {
        this(group, listenPort, true);
    }

    public TcpNettyDnsServer(EventLoopGroup group, int listenPort, boolean manageGroupLifecycle) {
        this.group = group;
        this.listenPort = listenPort;
        this.manageGroupLifecycle = manageGroupLifecycle;
    }

    @Override
    public DnsServer requestHandler(DnsRequestHandler handler) {
        this.requestHandler = handler;
        return this;
    }

    @Override
    public void start() throws DnsServerStartException {
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(group)
             .channel(NioServerSocketChannel.class)
             .option(ChannelOption.SO_REUSEADDR, true)
             .childHandler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 protected void initChannel(SocketChannel ch) {
                     ChannelPipeline p = ch.pipeline();
                     p.addLast("wireLogger", new LoggingHandler(LogLevel.DEBUG));
                     p.addLast(new DnsTcpDecoder());
                     p.addLast(new DnsTcpEncoder());
                     p.addLast(new TcpDnsHandler(requestHandler));
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

    @Override
    public void stop() {
        if (channel != null) {
            channel.close();
        }
        if (manageGroupLifecycle) {
            group.shutdownGracefully();
        }
    }
}
