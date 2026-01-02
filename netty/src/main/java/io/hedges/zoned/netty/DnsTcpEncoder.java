// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.netty;

import io.hedges.zoned.core.DnsMessageEncoder;
import io.hedges.zoned.core.dom.DnsMessageDom;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public final class DnsTcpEncoder extends MessageToByteEncoder<DnsMessageDom> {

    @Override
    protected void encode(ChannelHandlerContext ctx, DnsMessageDom msg, ByteBuf out) {
        ByteBuf payload = ctx.alloc().buffer();
        try {
            DnsMessageEncoder.encode(msg, new NettyWireWriter(payload));
            out.writeShort(payload.readableBytes());
            out.writeBytes(payload);
        } finally {
            payload.release();
        }
    }
}
