// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.netty;

import io.hedges.zoned.core.DnsRequestHandler;
import io.hedges.zoned.core.DnsServer;
import io.hedges.zoned.core.DnsServerStartException;
import io.netty.channel.EventLoopGroup;

public final class NettyDnsServer implements DnsServer {

    private final EventLoopGroup group;
    private final UdpNettyDnsServer udpServer;
    private final TcpNettyDnsServer tcpServer;

    public NettyDnsServer(EventLoopGroup group, int listenPort) {
        this.group = group;
        this.udpServer = new UdpNettyDnsServer(group, listenPort, false);
        this.tcpServer = new TcpNettyDnsServer(group, listenPort, false);
    }

    @Override
    public DnsServer requestHandler(DnsRequestHandler handler) {
        udpServer.requestHandler(handler);
        tcpServer.requestHandler(handler);
        return this;
    }

    @Override
    public void start() throws DnsServerStartException {
        udpServer.start();
        try {
            tcpServer.start();
        } catch (DnsServerStartException e) {
            udpServer.stop();
            group.shutdownGracefully();
            throw e;
        }
    }

    @Override
    public void stop() {
        tcpServer.stop();
        udpServer.stop();
        group.shutdownGracefully();
    }
}
