package io.hedges.zoned.netty;

import io.hedges.zoned.core.DnsMessageDecoder;
import io.hedges.zoned.core.dom.DnsMessageDom;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

public final class DnsDatagramDecoder extends SimpleChannelInboundHandler<DatagramPacket> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {
        DnsMessageDom message = DnsMessageDecoder.decode(new NettyWireReader(packet.content()));
        ctx.fireChannelRead(new UdpDnsInbound(message, packet.sender()));
    }
}
