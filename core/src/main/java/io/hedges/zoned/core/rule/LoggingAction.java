package io.hedges.zoned.core.rule;

import io.hedges.zoned.core.DnsExecutionContext;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.logging.Logger;

public final class LoggingAction implements DnsAction {

    private final Logger logger;

    public LoggingAction(Logger logger) {
        this.logger = logger;
    }

    @Override
    public CompletionStage<ActionOutcome> execute(DnsExecutionContext ctx) {
        var q = ctx.getRequest().getQuery().getQuestions().isEmpty()
                ? null
                : ctx.getRequest().getQuery().getQuestions().get(0);

        logger.info(() -> "DNS query from " + ctx.getRequest().getClientAddress()
                + " for " + (q != null ? q.getName() : "<none>")
                + " type " + (q != null ? q.getType() : "<none>"));

        return CompletableFuture.completedStage(ActionOutcome.CONTINUE);
    }
}
