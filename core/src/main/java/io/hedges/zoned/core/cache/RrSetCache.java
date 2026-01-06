// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.cache;

import io.hedges.zoned.core.dom.DnsQuestionDom;
import io.hedges.zoned.core.dom.DnsResourceRecordDom;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class RrSetCache {

    Cache<RrsetKey, CacheEntry> store = new EvictingCache<>();

    public RrSetCache() {
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

            // RFC says to take min ttl if the ttls in a rrset differ
            // https://www.rfc-editor.org/rfc/rfc2181#section-5.2
            int answerTtl = answers.stream().mapToInt(DnsResourceRecordDom::ttlSeconds).min().getAsInt();

            CacheEntry answerCacheEntry =
                    CacheEntry
                            .builder()
                            .complete(true)
                            .rrset(
                                    RrSet.builder().records(answers).build()
                            )
                            .expireAt(now + answerTtl)
                            .build();

            store.put(key, answerCacheEntry);
        }

        if (authorities != null && !authorities.isEmpty()) {
            int authoritiesTtl = authorities.stream().mapToInt(DnsResourceRecordDom::ttlSeconds).min().getAsInt();
            CacheEntry answerCacheEntry =
                    CacheEntry
                            .builder()
                            .complete(false)
                            .rrset(
                                    RrSet.builder().records(answers).build()
                            )
                            .expireAt(now + authoritiesTtl)
                            .build();

            store.put(key, answerCacheEntry);
        }

        if (additionals != null && !additionals.isEmpty()) {
            int additionalsTtl = additionals.stream().mapToInt(DnsResourceRecordDom::ttlSeconds).min().getAsInt();
            CacheEntry answerCacheEntry =
                    CacheEntry
                            .builder()
                            .complete(false)
                            .rrset(
                                    RrSet.builder().records(additionals).build()
                            )
                            .expireAt(now + additionalsTtl)
                            .build();

            store.put(key, answerCacheEntry);
        }


    }

    public void storeAnswer(DnsQuestionDom questionDom,
                            List<DnsResourceRecordDom> answers,
                            List<DnsResourceRecordDom> authorities,
                            List<DnsResourceRecordDom> additionals, long now) {
        storeAnswer(RrsetKey.fromQuestion(questionDom), answers, authorities, additionals, now);
    }

}
