// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.netty;

import io.hedges.zoned.core.DnsMessageDecoder;
import io.hedges.zoned.core.dom.DnsMessageDom;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public final class DnsTcpDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < 2) {
            return;
        }
        in.markReaderIndex();
        int length = in.readUnsignedShort();
        if (in.readableBytes() < length) {
            in.resetReaderIndex();
            return;
        }
        ByteBuf frame = in.readSlice(length);
        DnsMessageDom message = DnsMessageDecoder.decode(new NettyWireReader(frame));
        out.add(message);
    }
}
