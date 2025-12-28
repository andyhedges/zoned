// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.netty;

import io.hedges.zoned.core.DnsClient;
import io.hedges.zoned.core.DnsImplementationProvider;
import io.hedges.zoned.core.DnsServer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import lombok.Getter;

import java.net.InetSocketAddress;

@Getter
public class NettyDnsImplementationProvider implements DnsImplementationProvider {

    private final DnsServer server;
    private final DnsClient client;
    private final EventLoopGroup group;

    public NettyDnsImplementationProvider(int serverPort, InetSocketAddress upstream){
        this.group = new MultiThreadIoEventLoopGroup(
                Runtime.getRuntime().availableProcessors(),
                NioIoHandler.newFactory());
        this.server = new UdpNettyDnsServer(group, serverPort);
        this.client = new NettyDnsClient(group, upstream);

    }


}
