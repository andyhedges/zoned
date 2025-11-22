package io.hedges.zoned.core;

import java.util.concurrent.CompletionStage;

public interface DnsAction {

    enum ActionOutcome {
        CONTINUE,
        STOP
    }

    CompletionStage<ActionOutcome> execute(DnsExecutionContext ctx);
}
