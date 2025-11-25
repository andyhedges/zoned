package io.hedges.zoned.core.rule;

import io.hedges.zoned.core.DnsExecutionContext;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.logging.Logger;

public final class LoggingAction implements DnsAction {



    @Override
    public CompletionStage<ActionOutcome> execute(DnsExecutionContext ctx) {

        //do something
        return CompletableFuture.completedStage(ActionOutcome.CONTINUE);
    }
}
