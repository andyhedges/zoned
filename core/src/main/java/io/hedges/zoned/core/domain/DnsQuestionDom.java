package io.hedges.zoned.core.domain;

public record DnsQuestionDom(DnsName name, DnsRecordTypeDom type, DnsRecordClassDom recordClass) {
}
