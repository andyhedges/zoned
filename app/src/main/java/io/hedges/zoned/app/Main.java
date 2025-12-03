package io.hedges.zoned.app;

import io.hedges.zoned.core.DnsServer;
import io.hedges.zoned.core.dom.DnsHeaderDom;
import io.hedges.zoned.core.dom.DnsMessageDom;
import io.hedges.zoned.netty.NettyDnsMapper;
import io.hedges.zoned.netty.UdpNettyDnsServer;

public final class Main {
    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 53;
        DnsServer server = new UdpNettyDnsServer(port);
        server.requestHandler(rc -> {
            rc.response(
                    DnsMessageDom
                            .builder()
                            .header(
                                    DnsHeaderDom
                                            .builder()
                                            .id(rc.query().header().id())
                                            .build()
                            )
                            .build());
        }).start();
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        System.out.println("zoned DNS server listening on UDP " + port);
    }
}
