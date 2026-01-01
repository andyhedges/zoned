// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.netty;

import io.hedges.zoned.core.DnsRequestContext;
import io.hedges.zoned.core.DnsRequestHandler;
import io.hedges.zoned.core.dom.Transport;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

@Slf4j
@AllArgsConstructor
public final class UdpDnsHandler extends SimpleChannelInboundHandler<UdpDnsInbound> {

    private DnsRequestHandler requestHandler;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, UdpDnsInbound inbound) {
        DnsRequestContext drc = DnsRequestContext.builder()
                .query(inbound.message())
                .clientAddress(inbound.sender())
                .transport(Transport.UDP)
                .receivedAt(Instant.now())
                .build();

        requestHandler.handle(drc).whenComplete((response, t) -> {

            if (t != null) {
                log.error("Error processing DNS request", t); // TODO return appropriate protocol error
            }

            if (response == null) {
                return;
            }

            ctx.writeAndFlush(new UdpDnsOutbound(response, inbound.sender()));
        });
    }

}
