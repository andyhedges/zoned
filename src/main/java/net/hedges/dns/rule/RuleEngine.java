package net.hedges.dns.rule;

import io.netty.handler.codec.dns.DnsResponse;
import net.hedges.dns.DnsRequestContext;
import net.hedges.dns.DnsResponseEnvelope;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public final class RuleEngine {

    private final List<DnsRule> rules;

    public RuleEngine(List<DnsRule> rules) {
        this.rules = List.copyOf(rules);
    }

    public CompletionStage<Optional<DnsResponseEnvelope>> handle(DnsRequestContext ctx) {
        return applyRuleAtIndex(ctx, 0);
    }

    private CompletionStage<Optional<DnsResponseEnvelope>> applyRuleAtIndex(
            DnsRequestContext ctx, int index) {

        if (index >= rules.size()) {
            return CompletableFuture.completedStage(Optional.empty());
        }

        DnsRule rule = rules.get(index);
        if (!rule.matches(ctx)) {
            return applyRuleAtIndex(ctx, index + 1);
        }

        return rule.applyAsync(ctx, this).thenCompose(opt -> {
            if (opt.isPresent()) {
                return CompletableFuture.completedStage(opt);
            } else {
                return applyRuleAtIndex(ctx, index + 1);
            }
        });
    }
}


