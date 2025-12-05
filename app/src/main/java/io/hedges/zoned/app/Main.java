package io.hedges.zoned.app;

import io.hedges.zoned.core.DnsImplementationProvider;
import io.hedges.zoned.core.DnsServer;
import io.hedges.zoned.netty.NettyDnsImplementationProvider;

import java.net.InetSocketAddress;

public final class Main {
    public static void main(String[] args) throws Exception {
        InetSocketAddress upstream = new InetSocketAddress("one.one.one.one", 53);
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 53;
        DnsImplementationProvider dip = new NettyDnsImplementationProvider(53, upstream);
        DnsServer server = dip.server();
        server.requestHandler(new DefaultDnsRequestRouter(dip.client())).start();
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        System.out.println("zoned DNS server listening on UDP " + port);
    }
}
