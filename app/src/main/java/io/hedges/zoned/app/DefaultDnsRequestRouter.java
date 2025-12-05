package io.hedges.zoned.app;

import io.hedges.zoned.core.DnsRequestContext;
import io.hedges.zoned.core.DnsRequestRouter;
import io.hedges.zoned.core.dom.DnsHeaderDom;
import io.hedges.zoned.core.dom.DnsMessageDom;

public class DefaultDnsRequestRouter implements DnsRequestRouter {

    @Override
    public void handle(DnsRequestContext ctx) {

        ctx.response(
                DnsMessageDom
                        .builder()
                        .header(
                                DnsHeaderDom
                                        .builder()
                                        .id(ctx.query().header().id())
                                        .build()
                        )
                        .build());
    }
}
