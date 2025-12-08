package io.hedges.zoned.core.dom;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Collections;
import java.util.List;

@Getter
@Builder
@ToString
public class DnsMessageDom {

    private DnsHeaderDom header;
    @Builder.Default
    private List<DnsQuestionDom> questions = Collections.emptyList();
    @Builder.Default
    private List<DnsResourceRecordDom> answers = Collections.emptyList();
    @Builder.Default
    private List<DnsResourceRecordDom> authorities = Collections.emptyList();
    @Builder.Default
    private List<DnsResourceRecordDom> additionals = Collections.emptyList();
}
