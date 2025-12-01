package io.hedges.zoned.netty;

import io.hedges.zoned.core.DnsRequestContext;
import io.hedges.zoned.core.dom.Transport;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.dns.*;

public final class UdpDnsHandler extends SimpleChannelInboundHandler<DatagramDnsQuery> {


    public UdpDnsHandler() {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramDnsQuery query) {
        DnsRequestContext domReq = NettyDnsMapper.fromNetty(query, Transport.UDP);

        System.out.println(domReq);

        //using the routing pipeline
    }

    private DatagramDnsResponse createServfail(DatagramDnsQuery query) {
        DatagramDnsResponse resp = new DatagramDnsResponse(
                query.recipient(),
                query.sender(),
                query.id()
        );
        int qCount = query.count(DnsSection.QUESTION);
        for (int i = 0; i < qCount; i++) {
            DnsQuestion q = query.recordAt(DnsSection.QUESTION, i);
            resp.addRecord(DnsSection.QUESTION, q);
        }
        resp.setCode(DnsResponseCode.SERVFAIL);
        return resp;
    }
}
