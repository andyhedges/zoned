package io.hedges.zoned.core;

public final class DnsQuestionDom {
    private final DnsName name;
    private final DnsRecordTypeDom type;
    private final DnsRecordClassDom recordClass;

    public DnsQuestionDom(DnsName name,
                          DnsRecordTypeDom type,
                          DnsRecordClassDom recordClass) {
        this.name = name;
        this.type = type;
        this.recordClass = recordClass;
    }

    public DnsName getName() {
        return name;
    }

    public DnsRecordTypeDom getType() {
        return type;
    }

    public DnsRecordClassDom getRecordClass() {
        return recordClass;
    }
}
