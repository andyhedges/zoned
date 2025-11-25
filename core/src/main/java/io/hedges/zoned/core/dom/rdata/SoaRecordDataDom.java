package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.DnsNameDom;
import io.hedges.zoned.core.dom.RDataDom;

public class SoaRecordDataDom implements RDataDom {
    private DnsNameDom mname;
    private DnsNameDom rname;
    private long serial;
    private long refreshSeconds;
    private long retrySeconds;
    private long expireSeconds;
    private long minimumTtlSeconds;
}
