// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.resolver;

import io.hedges.zoned.core.DnsClient;
import io.hedges.zoned.core.DnsRequestContext;
import io.hedges.zoned.core.cache.CacheEntry;
import io.hedges.zoned.core.cache.RrSet;
import io.hedges.zoned.core.cache.RrSetCache;
import io.hedges.zoned.core.dom.DnsHeaderDom;
import io.hedges.zoned.core.dom.DnsMessageDom;
import io.hedges.zoned.core.dom.DnsNameDom;
import io.hedges.zoned.core.dom.DnsOpCodeDom;
import io.hedges.zoned.core.dom.DnsQuestionDom;
import io.hedges.zoned.core.dom.DnsRecordTypeDom;
import io.hedges.zoned.core.dom.DnsResourceRecordDom;
import io.hedges.zoned.core.dom.rdata.ARecordDataDom;
import io.hedges.zoned.core.dom.rdata.NsRecordDataDom;

import java.net.InetSocketAddress;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class IterativeResolver implements Resolver {

    private final DnsClient dnsClient;
    private final RrSetCache cache;

    public IterativeResolver(DnsClient dnsClient) {
        this(dnsClient, new RrSetCache());
    }

    public IterativeResolver(DnsClient dnsClient, RrSetCache cache) {
        this.dnsClient = Objects.requireNonNull(dnsClient, "dnsClient");
        this.cache = Objects.requireNonNull(cache, "cache");
    }

    @Override
    public CompletionStage<Resolution> resolve(DnsQuestionDom question, DnsRequestContext context) {
        Objects.requireNonNull(question, "question");
        long nowMillis = context == null ? System.currentTimeMillis() : context.receivedAt();
        return cache.lookup(question, nowMillis)
                .<CompletionStage<Resolution>>map(entry -> CompletableFuture.completedFuture(fromCache(entry)))
                .orElseGet(() -> resolveIteratively(question, nowMillis, rootServers()));
    }

    private static Resolution fromCache(CacheEntry entry) {
        RrSet rrset = entry.rrset();
        List<DnsResourceRecordDom> answers = rrset == null? List.of() : rrset.records();
        return new Resolution(answers, List.of(), List.of(), entry.complete());
    }

    private static DnsMessageDom buildQuery(DnsQuestionDom question) {
        DnsHeaderDom header = DnsHeaderDom.builder()
                                          .id(0)
                                          .response(false)
                                          .opCode(DnsOpCodeDom.QUERY)
                                          .recursionDesired(false)
                                          .build();
        return DnsMessageDom.builder()
                            .header(header)
                            .questions(List.of(question))
                            .build();
    }

    private CompletionStage<Resolution> resolveIteratively(DnsQuestionDom question,
                                                          long nowMillis,
                                                          List<InetSocketAddress> servers) {
        if (servers.isEmpty()) {
            return CompletableFuture.completedFuture(new Resolution(List.of(), List.of(), List.of(), false));
        }
        InetSocketAddress server = servers.getFirst();
        DnsMessageDom query = buildQuery(question);
        return dnsClient.send(query, server, null)
                .thenCompose(response -> {
                    if (response == null) {
                        return CompletableFuture.completedFuture(
                                new Resolution(List.of(), List.of(), List.of(), false));
                    }
                    List<DnsResourceRecordDom> answers = response.answers();
                    List<DnsResourceRecordDom> authorities = response.authorities();
                    List<DnsResourceRecordDom> additionals = response.additionals();
                    if (!answers.isEmpty() && answerAnswersQuestion(question, answers)) {
                        cache.storeAnswer(question, answers, authorities, additionals, nowMillis);
                        return CompletableFuture.completedFuture(
                                new Resolution(answers, authorities, additionals, true));
                    }
                    List<InetSocketAddress> nextServers = extractReferralAddresses(authorities, additionals);
                    if (nextServers.isEmpty()) {
                        return CompletableFuture.completedFuture(
                                new Resolution(List.of(), authorities, additionals, false));
                    }
                    return resolveIteratively(question, nowMillis, nextServers);
                });
    }

    private static List<InetSocketAddress> rootServers() {
        return List.of(new InetSocketAddress("198.41.0.4", 53));
    }

    private static boolean answerAnswersQuestion(DnsQuestionDom question, List<DnsResourceRecordDom> answers) {
        return answers.stream().allMatch(
                answer ->
                        question.recordType() == answer.type()
                        && question.recordClass() == answer.recordClass()
                        && question.name().equals(answer.name())
        );
    }

    private static List<InetSocketAddress> extractReferralAddresses(List<DnsResourceRecordDom> authorities,
                                                                    List<DnsResourceRecordDom> additionals) {
        if (authorities == null || additionals == null) {
            return List.of();
        }
        List<InetSocketAddress> servers = new ArrayList<>();
        for (DnsResourceRecordDom rr : authorities) {
            if (rr == null || rr.type() != DnsRecordTypeDom.NS || !(rr.rdata() instanceof NsRecordDataDom)) {
                continue;
            }
            DnsNameDom nsName = ((NsRecordDataDom) rr.rdata()).nsName();
            // I feel like we should ignore additional all together
            // or perhaps just accept ones in bailiwick
            for (DnsResourceRecordDom additional : additionals) {
                if (additional == null || additional.type() != DnsRecordTypeDom.A) {
                    continue;
                }
                if (!nsName.equals(additional.name())) {
                    continue;
                }
                if (additional.rdata() instanceof ARecordDataDom) {
                    ARecordDataDom a = (ARecordDataDom) additional.rdata();
                    if (a.address() != null) {
                        servers.add(new InetSocketAddress(a.address(), 53));
                    }
                }
            }
        }
        return servers;
    }

}
