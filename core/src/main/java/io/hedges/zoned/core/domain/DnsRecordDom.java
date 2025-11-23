package io.hedges.zoned.core.domain;

public final class DnsRecordDom {
    private final DnsName name;
    private final DnsRecordTypeDom type;
    private final DnsRecordClassDom recordClass;
    private final long ttlSeconds;
    private final byte[] rdata;

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

    public DnsName getName() {
        return name;
    }

    public DnsRecordTypeDom getType() {
        return type;
    }

    public DnsRecordClassDom getRecordClass() {
        return recordClass;
    }

    public long getTtlSeconds() {
        return ttlSeconds;
    }

    public byte[] getRdata() {
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
