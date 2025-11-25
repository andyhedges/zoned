package io.hedges.zoned.core.rule;

import io.hedges.zoned.core.DnsRequestContextDom;
import io.hedges.zoned.core.DnsResolver;

import java.util.concurrent.CompletionStage;

public interface DnsRule {

    boolean matches(DnsRequestContextDom ctx);

    CompletionStage<RuleResult> evaluate(DnsRequestContextDom ctx);
}
