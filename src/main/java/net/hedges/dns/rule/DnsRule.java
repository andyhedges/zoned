package net.hedges.dns.rule;

import io.netty.handler.codec.dns.DnsResponse;
import net.hedges.dns.DnsRequestContext;
import net.hedges.dns.DnsResponseEnvelope;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface DnsRule {

    boolean matches(DnsRequestContext ctx);

    CompletionStage<Optional<DnsResponseEnvelope>> applyAsync(
            DnsRequestContext ctx,
            RuleEngine engine
    );

}
