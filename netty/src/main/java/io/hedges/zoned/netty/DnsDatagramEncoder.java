package io.hedges.zoned.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

public final class DnsDatagramEncoder extends MessageToMessageEncoder<UdpDnsOutbound> {

    @Override
    protected void encode(ChannelHandlerContext ctx, UdpDnsOutbound msg, List<Object> out) {
        ByteBuf buf = DnsWireCodec.encode(msg.message(), ctx.alloc());
        out.add(new DatagramPacket(buf, msg.recipient()));
    }
}
