// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.cache;

import io.hedges.zoned.core.dom.DnsQuestionDom;
import io.hedges.zoned.core.dom.DnsResourceRecordDom;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class RRSetCache {

    Cache<RrsetKey, CacheEntry> store = new EvictingCache<>();

    public RRSetCache(){
        store.validityPolicy((v) -> {
            return v.expireAt() > System.currentTimeMillis();
        });
    }

    public Optional<CacheEntry> lookup(DnsQuestionDom questionDom, Instant now) {
        RrsetKey key = RrsetKey.fromQuestion(questionDom);
        return store.get(key);
    }

    public void storeAnswer(RrsetKey key,
                     List<DnsResourceRecordDom> answers,
                     List<DnsResourceRecordDom> authorities,
                     List<DnsResourceRecordDom> additionals, long now) {

        if (answers != null && !answers.isEmpty()) {
            //RFC says to take min ttl if the ttls in a rrset differ
            int answerTtl = answers.stream().mapToInt(rr -> (int) rr.ttlSeconds()).min().getAsInt();

            CacheEntry answerCacheEntry =
                    CacheEntry
                            .builder()
                            .complete(true)
                            .rrset(
                                    Rrset.builder().records(answers).build()
                            )
                            .trustLevel(TrustLevel.AUTHORITATIVE_ANSWER)
                            .expireAt(now + answerTtl)
                            .build();

            store.put(key, answerCacheEntry);
        }

        // TODO: Implement authorities/additionals.
    }

    public void storeAnswer(DnsQuestionDom questionDom,
                     List<DnsResourceRecordDom> answers,
                     List<DnsResourceRecordDom> authorities,
                     List<DnsResourceRecordDom> additionals, long now) {
        storeAnswer(RrsetKey.fromQuestion(questionDom), answers, authorities, additionals, now);
    }

}
