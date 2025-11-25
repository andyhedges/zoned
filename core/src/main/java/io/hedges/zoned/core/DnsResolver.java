package io.hedges.zoned.core;

import io.hedges.zoned.core.dom.DnsMessageDom;
import java.util.concurrent.CompletionStage;

/**
 * A resolver capable of handling an incoming DNS request and producing a DNS response.
 */
public interface DnsResolver {

    /**
     * Resolves the given DNS request asynchronously.
     *
     * @param request the request context containing the parsed DNS query and metadata
     * @return a {@link CompletionStage} that completes with the DNS response message
     */
    CompletionStage<DnsMessageDom> resolve(DnsRequestContextDom request);

}
