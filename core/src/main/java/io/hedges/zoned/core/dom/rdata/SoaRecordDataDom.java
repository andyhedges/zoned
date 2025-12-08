package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.DnsNameDom;
import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class SoaRecordDataDom implements RDataDom {
    private DnsNameDom mname;
    private DnsNameDom rname;
    private long serial;
    private long refreshSeconds;
    private long retrySeconds;
    private long expireSeconds;
    private long minimumTtlSeconds;

    @Override
    public void from(byte[] raw) {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public byte[] to() {
        throw new RuntimeException("Not Implemented");
    }
}
