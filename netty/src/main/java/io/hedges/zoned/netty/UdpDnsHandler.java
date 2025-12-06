package io.hedges.zoned.netty;

import io.hedges.zoned.core.DnsRequestContext;
import io.hedges.zoned.core.DnsRequestHandler;
import io.hedges.zoned.core.dom.DnsMessageDom;
import io.hedges.zoned.core.dom.Transport;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.dns.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
@AllArgsConstructor
public final class UdpDnsHandler extends SimpleChannelInboundHandler<DatagramDnsQuery> {

    private DnsRequestHandler requestHandler;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramDnsQuery query) {
        DnsRequestContext drc = NettyDnsMapper.fromNetty(query, Transport.UDP);

        requestHandler.handle(drc).whenComplete((response, t) -> {

            if (response == null) {
                return;
            }

            InetSocketAddress sender = query.recipient();
            InetSocketAddress recipient = query.sender();

            DatagramDnsResponse nettyResponse = NettyDnsMapper.toNettyResponse(response, sender, recipient);

            ctx.writeAndFlush(nettyResponse);
        });
    }


}
