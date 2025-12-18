package io.hedges.zoned.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

public final class DnsDatagramDecoder extends SimpleChannelInboundHandler<DatagramPacket> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {
        ctx.fireChannelRead(new UdpDnsInbound(
                DnsWireCodec.decode(packet.content()),
                packet.sender()));
    }
}
