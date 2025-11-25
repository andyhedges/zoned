package io.hedges.zoned.core.domain;

public record DnsRecordDom(DnsName name, DnsRecordTypeDom type, DnsRecordClassDom recordClass, long ttlSeconds,
                           byte[] rdata) {
    public DnsRecordDom(DnsName name,
                        DnsRecordTypeDom type,
                        DnsRecordClassDom recordClass,
                        long ttlSeconds,
                        byte[] rdata) {
        this.name = name;
        this.type = type;
        this.recordClass = recordClass;
        this.ttlSeconds = ttlSeconds;
        this.rdata = rdata != null ? rdata.clone() : new byte[0];
    }

    @Override
    public byte[] rdata() {
        return rdata.clone();
    }

    @Override
    public String toString() {
        return "DnsRecordDom{" +
                "name=" + name +
                ", type=" + type +
                ", class=" + recordClass +
                ", ttlSeconds=" + ttlSeconds +
                ", rdata.length=" + rdata.length +
                '}';
    }
}
