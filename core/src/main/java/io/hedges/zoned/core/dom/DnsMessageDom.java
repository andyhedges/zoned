package io.hedges.zoned.core.dom;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder
@ToString
public class DnsMessageDom {

    private DnsHeaderDom header;
    private List<DnsQuestionDom> questions;
    private List<DnsResourceRecordDom> answers;
    private List<DnsResourceRecordDom> authorities;
    private List<DnsResourceRecordDom> additionals;
}
