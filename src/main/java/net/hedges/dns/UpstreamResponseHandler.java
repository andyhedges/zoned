package net.hedges.dns;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.dns.DatagramDnsResponse;

import java.util.concurrent.ConcurrentMap;

final class UpstreamResponseHandler
        extends SimpleChannelInboundHandler<DatagramDnsResponse> {

    private final ConcurrentMap<Integer, PendingRequest> pending;

    UpstreamResponseHandler(ConcurrentMap<Integer, PendingRequest> pending) {
        this.pending = pending;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramDnsResponse msg) {
        int upstreamId = msg.id();
        PendingRequest pr = pending.remove(upstreamId);
        if (pr == null) {
            return; // late or unknown response
        }

        pr.timeoutTask.cancel(false);

        // restore original ID inside the envelope
        msg.setId(pr.originalId);

        DnsResponseEnvelope env = DnsResponseEnvelope.fromNetty(msg);
        pr.future.complete(env);
    }
}

