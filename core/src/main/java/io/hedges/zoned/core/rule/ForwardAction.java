package io.hedges.zoned.core.rule;

import io.hedges.zoned.core.DnsExecutionContext;
import io.hedges.zoned.core.DnsResolver;

import java.util.concurrent.CompletionStage;

public final class ForwardAction implements DnsAction {

    private final DnsResolver resolver;

    public ForwardAction(DnsResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public CompletionStage<ActionOutcome> execute(DnsExecutionContext ctx) {
        return resolver.resolve(ctx.getRequest())
                .thenApply(resp -> {
                    ctx.setResponse(resp);
                    return ActionOutcome.STOP;
                })
                .exceptionally(ex -> ActionOutcome.STOP);
    }
}
