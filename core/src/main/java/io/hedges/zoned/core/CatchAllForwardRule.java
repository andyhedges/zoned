package io.hedges.zoned.core;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public final class CatchAllForwardRule implements DnsRule {

    private final DnsResolver resolver;

    public CatchAllForwardRule(DnsResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public boolean matches(DnsRequestContextDom ctx) {
        return true;
    }

    @Override
    public CompletionStage<RuleResult> evaluate(DnsRequestContextDom ctx) {
        DnsAction forward = new ForwardAction(resolver);
        return CompletableFuture.completedStage(
                new RuleResult(
                        List.of(forward),
                        RuleResult.Disposition.TERMINATE,
                        100
                )
        );
    }
}
