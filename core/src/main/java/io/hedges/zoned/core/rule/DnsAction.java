package io.hedges.zoned.core.rule;

import io.hedges.zoned.core.DnsExecutionContext;

import java.util.concurrent.CompletionStage;

public interface DnsAction {

    enum ActionOutcome {
        CONTINUE,
        STOP
    }

    CompletionStage<ActionOutcome> execute(DnsExecutionContext ctx);
}
