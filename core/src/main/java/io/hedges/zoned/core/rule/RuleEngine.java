package io.hedges.zoned.core.rule;

import io.hedges.zoned.core.DnsRequestContextDom;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public final class RuleEngine {

    private final List<DnsRule> rules;

    public RuleEngine(List<DnsRule> rules) {
        this.rules = List.copyOf(rules);
    }

    public CompletionStage<List<DnsAction>> buildActionPlan(DnsRequestContextDom ctx) {
        List<RuleResult> results = new ArrayList<>();
        CompletableFuture<List<DnsAction>> out = new CompletableFuture<>();
        applyRuleAtIndex(ctx, 0, results, out);
        return out;
    }

    private void applyRuleAtIndex(DnsRequestContextDom ctx,
                                  int index,
                                  List<RuleResult> results,
                                  CompletableFuture<List<DnsAction>> out) {
        if (index >= rules.size()) {
            List<DnsAction> actions = results.stream()
                    .sorted(Comparator.comparingInt(RuleResult::priority))
                    .flatMap(r -> r.actions().stream())
                    .toList();
            out.complete(actions);
            return;
        }

        DnsRule rule = rules.get(index);
        if (!rule.matches(ctx)) {
            applyRuleAtIndex(ctx, index + 1, results, out);
            return;
        }

        rule.evaluate(ctx).whenComplete((rr, error) -> {
            if (error == null && rr != null) {
                results.add(rr);
                if (rr.disposition() == RuleResult.Disposition.TERMINATE) {
                    List<DnsAction> actions = results.stream()
                            .sorted(Comparator.comparingInt(RuleResult::priority))
                            .flatMap(r -> r.actions().stream())
                            .toList();
                    out.complete(actions);
                    return;
                }
            }
            applyRuleAtIndex(ctx, index + 1, results, out);
        });
    }
}
