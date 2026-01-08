// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.resolver;

import io.hedges.zoned.core.dom.DnsQuestionDom;
import io.hedges.zoned.core.dom.DnsResourceRecordDom;

import java.util.List;
import java.util.concurrent.CompletionStage;

@FunctionalInterface
public interface Resolver {

    CompletionStage<Resolution> resolve(DnsQuestionDom question, long nowMillis);

    default CompletionStage<Resolution> resolve(DnsQuestionDom question) {
        return resolve(question, System.currentTimeMillis());
    }

    record Resolution(List<DnsResourceRecordDom> answers,
                      List<DnsResourceRecordDom> authorities,
                      List<DnsResourceRecordDom> additionals,
                      boolean complete) {
    }
}
