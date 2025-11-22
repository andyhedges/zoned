package io.hedges.zoned.core;

import java.util.List;

public final class DnsMessageDom {
    private final int id;
    private final boolean recursionDesired;
    private final boolean recursionAvailable;
    private final boolean authoritativeAnswer;
    private final DnsResponseCodeDom rcode;

    private final List<DnsQuestionDom> questions;
    private final List<DnsRecordDom> answers;
    private final List<DnsRecordDom> authorities;
    private final List<DnsRecordDom> additionals;

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

    public int getId() {
        return id;
    }

    public boolean isRecursionDesired() {
        return recursionDesired;
    }

    public boolean isRecursionAvailable() {
        return recursionAvailable;
    }

    public boolean isAuthoritativeAnswer() {
        return authoritativeAnswer;
    }

    public DnsResponseCodeDom getRcode() {
        return rcode;
    }

    public List<DnsQuestionDom> getQuestions() {
        return questions;
    }

    public List<DnsRecordDom> getAnswers() {
        return answers;
    }

    public List<DnsRecordDom> getAuthorities() {
        return authorities;
    }

    public List<DnsRecordDom> getAdditionals() {
        return additionals;
    }
}
