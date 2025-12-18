package io.hedges.zoned.netty;

import io.hedges.zoned.core.dom.DnsMessageDom;

import java.net.InetSocketAddress;

public record UdpDnsInbound(DnsMessageDom message, InetSocketAddress sender) {
}
