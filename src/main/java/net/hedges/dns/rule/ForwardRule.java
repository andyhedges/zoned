package net.hedges.dns.rule;

import net.hedges.dns.DnsBackend;
import net.hedges.dns.DnsRequestContext;
import net.hedges.dns.DnsResponseEnvelope;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

public final class ForwardRule implements DnsRule {

    private final DnsBackend backend;

    public ForwardRule(DnsBackend backend) {
        this.backend = backend;
    }

    @Override
    public boolean matches(DnsRequestContext ctx) {
        // catch all for now
        return true;
    }

    @Override
    public CompletionStage<Optional<DnsResponseEnvelope>> applyAsync(
            DnsRequestContext ctx,
            RuleEngine engine) {

        return backend.resolve(ctx)
                .thenApply(resp -> Optional.of(resp))
                .exceptionally(ex -> {
                    // swallow and indicate we did not handle
                    return Optional.empty();
                });
    }
}