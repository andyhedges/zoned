package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.DnsNameDom;
import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class SrvRecordDataDom implements RDataDom {
    private int priority;
    private int weight;
    private int port;
    private DnsNameDom target;
}
