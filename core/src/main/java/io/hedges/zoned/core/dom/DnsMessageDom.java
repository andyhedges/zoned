package io.hedges.zoned.core.dom;

import lombok.Getter;

import java.util.List;

@Getter
public class DnsMessageDom {

    private DnsHeaderDom header;
    private List<DnsQuestionDom> questions;
    private List<DnsResourceRecordDom> answers;
    private List<DnsResourceRecordDom> authorities;
    private List<DnsResourceRecordDom> additionals;
}
