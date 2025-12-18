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

    public static RDataDom from(byte[] rdata) {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }

    @Override
    public byte[] to() {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }
}
