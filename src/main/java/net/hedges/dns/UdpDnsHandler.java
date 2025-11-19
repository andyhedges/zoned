package net.hedges.dns;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.dns.*;
import net.hedges.dns.rule.RuleEngine;

import java.net.SocketAddress;
import java.time.Instant;

public final class UdpDnsHandler extends SimpleChannelInboundHandler<DatagramDnsQuery> {

    private final RuleEngine ruleEngine;

    public UdpDnsHandler(RuleEngine ruleEngine) {
        this.ruleEngine = ruleEngine;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramDnsQuery query) {
        SocketAddress client = query.sender();
        DnsQuestion question = query.recordAt(DnsSection.QUESTION);

        DnsRequestContext rctx = new DnsRequestContext(
                client,
                question,
                query,
                Instant.now()
        );

        ruleEngine.handle(rctx).whenComplete((maybeResp, error) -> {
            DatagramDnsResponse wireResp;

            if (error != null) {
                wireResp = createServfail(query, question);
            } else if (maybeResp.isEmpty()) {
                wireResp = createServfail(query, question);
            } else {
                wireResp = toWireResponse(query, maybeResp.get());
            }

            ctx.writeAndFlush(wireResp);
        });
    }

    private DatagramDnsResponse createServfail(DatagramDnsQuery query, DnsQuestion q) {
        DatagramDnsResponse resp = new DatagramDnsResponse(
                query.recipient(),
                query.sender(),
                query.id()
        );
        resp.addRecord(DnsSection.QUESTION, q);
        resp.setCode(DnsResponseCode.SERVFAIL);
        return resp;
    }

    private DatagramDnsResponse toWireResponse(DatagramDnsQuery query, DnsResponseEnvelope e) {
        DatagramDnsResponse resp = new DatagramDnsResponse(
                query.recipient(),
                query.sender(),
                query.id()    // restore original ID
        );
        resp.addRecord(DnsSection.QUESTION, e.getQuestion());

        e.getAnswers().forEach(r -> resp.addRecord(DnsSection.ANSWER, r));
        e.getAuthorities().forEach(r -> resp.addRecord(DnsSection.AUTHORITY, r));
        e.getAdditionals().forEach(r -> resp.addRecord(DnsSection.ADDITIONAL, r));

        resp.setCode(e.getRcode());
        return resp;
    }
}