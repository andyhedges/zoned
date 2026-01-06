// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.resolver;

import io.hedges.zoned.core.DnsClient;
import io.hedges.zoned.core.DnsRequestContext;
import io.hedges.zoned.core.cache.CacheEntry;
import io.hedges.zoned.core.cache.RrSet;
import io.hedges.zoned.core.cache.RrSetCache;
import io.hedges.zoned.core.dom.DnsHeaderDom;
import io.hedges.zoned.core.dom.DnsMessageDom;
import io.hedges.zoned.core.dom.DnsOpCodeDom;
import io.hedges.zoned.core.dom.DnsQuestionDom;
import io.hedges.zoned.core.dom.DnsResourceRecordDom;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class RecursiveResolver implements Resolver {

    private final DnsClient dnsClient;
    private final RrSetCache cache;

    public RecursiveResolver(DnsClient dnsClient) {
        this(dnsClient, new RrSetCache());
    }

    public RecursiveResolver(DnsClient dnsClient, RrSetCache cache) {
        this.dnsClient = Objects.requireNonNull(dnsClient, "dnsClient");
        this.cache = Objects.requireNonNull(cache, "cache");
    }

    @Override
    public CompletionStage<Resolution> resolve(DnsQuestionDom question, DnsRequestContext context) {
        Objects.requireNonNull(question, "question");
        long nowMillis = requireNowMillis(context);
        Instant now = Instant.ofEpochMilli(nowMillis);
        return cache.lookup(question, now)
                .<CompletionStage<Resolution>>map(entry -> CompletableFuture.completedFuture(fromCache(entry)))
                .orElseGet(() -> resolveViaClient(question, nowMillis));
    }

    private CompletionStage<Resolution> resolveViaClient(DnsQuestionDom question, long nowMillis) {
        DnsMessageDom query = buildQuery(question);
        return dnsClient.send(query)
                .thenApply(response -> {
                    List<DnsResourceRecordDom> answers = safeRecords(response.answers());
                    List<DnsResourceRecordDom> authorities = safeRecords(response.authorities());
                    List<DnsResourceRecordDom> additionals = safeRecords(response.additionals());
                    cache.storeAnswer(question, answers, authorities, additionals, nowMillis);
                    return new Resolution(answers, authorities, additionals, !answers.isEmpty());
                });
    }

    private static Resolution fromCache(CacheEntry entry) {
        RrSet rrset = entry.rrset();
        List<DnsResourceRecordDom> answers = rrset == null ? List.of() : safeRecords(rrset.records());
        return new Resolution(answers, List.of(), List.of(), entry.complete());
    }

    private static DnsMessageDom buildQuery(DnsQuestionDom question) {
        DnsHeaderDom header = DnsHeaderDom.builder()
                .id(0)
                .response(false)
                .opCode(DnsOpCodeDom.QUERY)
                .recursionDesired(true)
                .build();
        return DnsMessageDom.builder()
                .header(header)
                .questions(List.of(question))
                .build();
    }

    private static List<DnsResourceRecordDom> safeRecords(List<DnsResourceRecordDom> records) {
        return records == null ? List.of() : records;
    }

    private static long requireNowMillis(DnsRequestContext context) {
        if (context == null || context.receivedAt() <= 0) {
            throw new IllegalArgumentException("context.receivedAt is required");
        }
        return context.receivedAt();
    }
}
