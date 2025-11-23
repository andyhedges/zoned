package io.hedges.zoned.core.rule;

import io.hedges.zoned.core.DnsExecutionContext;
import io.hedges.zoned.core.domain.DnsMessageDom;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public final class ActionExecutor {

    public CompletionStage<Optional<DnsMessageDom>> executeAll(
            List<DnsAction> actions,
            DnsExecutionContext ctx) {

        CompletableFuture<Optional<DnsMessageDom>> result = new CompletableFuture<>();
        runActionAtIndex(actions, ctx, 0, result);
        return result;
    }

    private void runActionAtIndex(
            List<DnsAction> actions,
            DnsExecutionContext ctx,
            int index,
            CompletableFuture<Optional<DnsMessageDom>> result) {

        if (index >= actions.size()) {
            result.complete(ctx.getResponse());
            return;
        }

        DnsAction action = actions.get(index);
        action.execute(ctx).whenComplete((outcome, error) -> {
            if (error != null) {
                result.complete(ctx.getResponse());
                return;
            }

            if (outcome == DnsAction.ActionOutcome.STOP || ctx.getResponse().isPresent()) {
                result.complete(ctx.getResponse());
            } else {
                runActionAtIndex(actions, ctx, index + 1, result);
            }
        });
    }
}
