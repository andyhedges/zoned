// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.cache;

import io.hedges.zoned.core.dom.DnsNameDom;
import io.hedges.zoned.core.dom.DnsQuestionDom;
import io.hedges.zoned.core.dom.DnsRecordClassDom;
import io.hedges.zoned.core.dom.DnsRecordTypeDom;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@EqualsAndHashCode
@ToString
public class RrsetKey {
    private DnsNameDom name;
    private DnsRecordTypeDom type;
    private DnsRecordClassDom recordClass;

    /**
     * Build an RRset key from a DNS question.
     */
    public static RrsetKey fromQuestion(DnsQuestionDom question) {
        return RrsetKey.builder()
                .name(question.name())
                .type(question.recordType())
                .recordClass(question.recordClass())
                .build();
    }
}
