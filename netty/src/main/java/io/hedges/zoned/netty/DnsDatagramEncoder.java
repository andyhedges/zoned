package io.hedges.zoned.netty;

import io.hedges.zoned.core.DnsMessageEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

public final class DnsDatagramEncoder extends MessageToMessageEncoder<UdpDnsOutbound> {

    @Override
    protected void encode(ChannelHandlerContext ctx, UdpDnsOutbound msg, List<Object> out) {
        ByteBuf buf = ctx.alloc().buffer();
        DnsMessageEncoder.encode(msg.message(), new NettyWireWriter(buf));
        out.add(new DatagramPacket(buf, msg.recipient()));
    }
}
