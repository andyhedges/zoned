package io.hedges.zoned.core.rule;

import io.hedges.zoned.core.domain.DnsRequestContextDom;

import java.util.concurrent.CompletionStage;

public interface DnsRule {

    boolean matches(DnsRequestContextDom ctx);

    CompletionStage<RuleResult> evaluate(DnsRequestContextDom ctx);
}
