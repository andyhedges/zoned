package io.hedges.zoned.netty;

import io.hedges.zoned.core.DnsRequestContextDom;
import io.hedges.zoned.core.dom.DnsMessageDom;
import io.netty.handler.codec.dns.DatagramDnsQuery;
import io.netty.handler.codec.dns.DatagramDnsResponse;

import java.net.InetSocketAddress;

public class NettyDnsMapper {

    // Inbound
    public static DnsRequestContextDom fromNetty(DatagramDnsQuery nettyQuery) {
        return null;
    }

    public static  DnsRequestContextDom fromNetty(DatagramDnsResponse nettyResponse) {
        return null;
    }

    // Outbound
    public static  DatagramDnsQuery toNettyQuery(DnsMessageDom dom, InetSocketAddress sender, InetSocketAddress recipient) {
        return null;
    }

    public static DatagramDnsResponse toNettyResponse(DnsMessageDom dom, InetSocketAddress sender, InetSocketAddress recipient) {
        return null;
    }
}
