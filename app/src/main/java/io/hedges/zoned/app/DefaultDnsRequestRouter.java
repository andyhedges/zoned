// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.app;

import io.hedges.zoned.core.DnsClient;
import io.hedges.zoned.core.DnsRequestContext;
import io.hedges.zoned.core.DnsRequestRouter;
import io.hedges.zoned.core.dom.*;
import lombok.AllArgsConstructor;

import java.util.concurrent.CompletionStage;

@AllArgsConstructor
public class DefaultDnsRequestRouter implements DnsRequestRouter {

    private DnsClient client;

    @Override
    public CompletionStage<DnsMessageDom> handle(DnsRequestContext ctx) {
        return client.send(ctx.query());
    }
}
