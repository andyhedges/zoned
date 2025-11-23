package io.hedges.zoned.netty;

import io.hedges.zoned.core.rule.ActionExecutor;
import io.hedges.zoned.core.DnsExecutionContext;
import io.hedges.zoned.core.domain.DnsRequestContextDom;
import io.hedges.zoned.core.rule.RuleEngine;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.dns.*;

public final class UdpDnsHandler extends SimpleChannelInboundHandler<DatagramDnsQuery> {

    private final RuleEngine ruleEngine;
    private final ActionExecutor actionExecutor;

    public UdpDnsHandler(RuleEngine ruleEngine, ActionExecutor actionExecutor) {
        this.ruleEngine = ruleEngine;
        this.actionExecutor = actionExecutor;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramDnsQuery query) {
        DnsRequestContextDom domReq = NettyDnsMapper.toDomain(query);
        DnsExecutionContext execCtx = new DnsExecutionContext(domReq);

        ruleEngine.buildActionPlan(domReq)
                  .thenCompose(actions -> actionExecutor.executeAll(actions, execCtx))
                  .whenComplete((maybeResp, error) -> {
                      DatagramDnsResponse wireResp;
                      if (error != null || execCtx.getResponse().isEmpty()) {
                          wireResp = createServfail(query);
                      } else {
                          wireResp = NettyDnsMapper.toNetty(execCtx.getResponse().get(), query);
                      }
                      ctx.writeAndFlush(wireResp);
                  });
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
