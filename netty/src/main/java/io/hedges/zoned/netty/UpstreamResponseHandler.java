package io.hedges.zoned.netty;

import io.hedges.zoned.core.dom.DnsMessageDom;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.dns.DatagramDnsResponse;

import java.util.concurrent.ConcurrentMap;

final class UpstreamResponseHandler extends SimpleChannelInboundHandler<DatagramDnsResponse> {

    private final ConcurrentMap<Integer, PendingRequest> pending;

    UpstreamResponseHandler(ConcurrentMap<Integer, PendingRequest> pending) {
        super(true);
        this.pending = pending;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramDnsResponse msg) {
//        int upstreamId = msg.id();
//        PendingRequest pr = pending.remove(upstreamId);
//        if (pr == null) {
//            return;
//        }
//        pr.timeoutTask.cancel(false);
//
//        DnsMessageDom dom = NettyDnsMapper.toDomainResponse(msg, pr.originalId);
//        pr.future.complete(dom);
    }
}
