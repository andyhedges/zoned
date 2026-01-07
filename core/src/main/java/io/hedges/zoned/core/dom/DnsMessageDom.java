// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.Collections;
import java.util.List;

@Getter
@Builder
@ToString
public class DnsMessageDom {

    private DnsHeaderDom header;

    @NonNull
    @Builder.Default
    private final List<DnsQuestionDom> questions = Collections.emptyList();

    @NonNull
    @Builder.Default
    private final List<DnsResourceRecordDom> answers = Collections.emptyList();

    @NonNull
    @Builder.Default
    private final List<DnsResourceRecordDom> authorities = Collections.emptyList();

    @NonNull
    @Builder.Default
    private final List<DnsResourceRecordDom> additionals = Collections.emptyList();
}
