package io.hedges.zoned.core.domain;

import java.util.List;

public record DnsMessageDom(int id, boolean recursionDesired, boolean recursionAvailable, boolean authoritativeAnswer,
                            DnsResponseCodeDom rcode, List<DnsQuestionDom> questions, List<DnsRecordDom> answers,
                            List<DnsRecordDom> authorities, List<DnsRecordDom> additionals) {
    public DnsMessageDom(int id,
                         boolean recursionDesired,
                         boolean recursionAvailable,
                         boolean authoritativeAnswer,
                         DnsResponseCodeDom rcode,
                         List<DnsQuestionDom> questions,
                         List<DnsRecordDom> answers,
                         List<DnsRecordDom> authorities,
                         List<DnsRecordDom> additionals) {
        this.id = id;
        this.recursionDesired = recursionDesired;
        this.recursionAvailable = recursionAvailable;
        this.authoritativeAnswer = authoritativeAnswer;
        this.rcode = rcode;
        this.questions = List.copyOf(questions);
        this.answers = List.copyOf(answers);
        this.authorities = List.copyOf(authorities);
        this.additionals = List.copyOf(additionals);
    }
}
