package io.hedges.zoned.core;

import java.util.concurrent.CompletionStage;

public interface DnsResolver {
    CompletionStage<DnsMessageDom> resolve(DnsRequestContextDom request);
}
