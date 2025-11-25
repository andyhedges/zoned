package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.DnsNameDom;
import io.hedges.zoned.core.dom.RDataDom;

public class SrvRecordDataDom implements RDataDom {
    private int priority;
    private int weight;
    private int port;
    private DnsNameDom target;
}
