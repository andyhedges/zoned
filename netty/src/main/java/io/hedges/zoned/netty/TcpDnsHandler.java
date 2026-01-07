// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.netty;

import io.hedges.zoned.core.DnsRequestContext;
import io.hedges.zoned.core.DnsRequestHandler;
import io.hedges.zoned.core.dom.DnsMessageDom;
import io.hedges.zoned.core.dom.Transport;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
@AllArgsConstructor
public final class TcpDnsHandler extends SimpleChannelInboundHandler<DnsMessageDom> {

    private DnsRequestHandler requestHandler;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DnsMessageDom message) {
        InetSocketAddress clientAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        DnsRequestContext drc = DnsRequestContext.builder()
                .query(message)
                .clientAddress(clientAddress)
                .transport(Transport.TCP)
                .receivedAt(System.currentTimeMillis())
                .build();

        requestHandler.handle(drc).whenComplete((response, t) -> {
            if (t != null) {
                log.error("Error processing DNS request", t); // TODO return appropriate protocol error
            }

            if (response == null) {
                return;
            }

            ctx.writeAndFlush(response);
        });
    }
}
