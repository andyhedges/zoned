package io.hedges.zoned.core;

import java.util.concurrent.CompletionStage;

public interface DnsRule {

    boolean matches(DnsRequestContextDom ctx);

    CompletionStage<RuleResult> evaluate(DnsRequestContextDom ctx);
}
