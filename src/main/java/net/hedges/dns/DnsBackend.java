package net.hedges.dns;

import java.util.concurrent.CompletionStage;

public interface DnsBackend {
        CompletionStage<DnsResponseEnvelope> resolve(DnsRequestContext ctx);
}
