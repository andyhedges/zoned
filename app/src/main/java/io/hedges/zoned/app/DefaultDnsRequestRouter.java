// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.app;

import io.hedges.zoned.core.DnsClient;
import io.hedges.zoned.core.DnsRequestContext;
import io.hedges.zoned.core.DnsRequestRouter;
import io.hedges.zoned.core.dom.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class DefaultDnsRequestRouter implements DnsRequestRouter {

    private DnsClient client;

    @Override
    public CompletionStage<DnsMessageDom> handle(DnsRequestContext ctx) {
        DnsMessageDom query = ctx.query();
        return client.send(query, ctx.transport()).whenComplete((response, t) -> {
            String questionSummary = formatQuestions(query);
            if (t != null) {
                log.error("dns_request route=forward question={} error=failed", questionSummary, t);
                return;
            }

            if (response == null) {
                log.info("dns_request route=forward question={} response=none", questionSummary);
                return;
            }

            log.info("dns_request route=forward question={} response={}", questionSummary, formatResponse(response));
        });
    }

    private static String formatQuestions(DnsMessageDom query) {
        if (query == null || query.questions() == null || query.questions().isEmpty()) {
            return "questions=0";
        }
        return query.questions().stream()
                .map(DefaultDnsRequestRouter::formatQuestion)
                .collect(Collectors.joining(", "));
    }

    private static String formatQuestion(DnsQuestionDom question) {
        if (question == null) {
            return "question=unknown";
        }
        String name = formatName(question.name());
        String type = question.recordType() == null ? "UNKNOWN" : question.recordType().name();
        String recordClass = question.recordClass() == null ? "UNKNOWN" : question.recordClass().name();
        return "name=" + name + " type=" + type + " class=" + recordClass;
    }

    private static String formatName(DnsNameDom name) {
        if (name == null || name.labels() == null || name.labels().isEmpty()) {
            return ".";
        }
        return String.join(".", name.labels());
    }

    private static String formatResponse(DnsMessageDom response) {
        String code = "UNKNOWN";
        if (response.header() != null && response.header().responseCode() != null) {
            code = response.header().responseCode().name();
        }
        int answerCount = response.answers() == null ? 0 : response.answers().size();
        int authorityCount = response.authorities() == null ? 0 : response.authorities().size();
        int additionalCount = response.additionals() == null ? 0 : response.additionals().size();
        return "rcode=" + code + " answers=" + answerCount + " authorities=" + authorityCount + " additionals=" + additionalCount;
    }
}
