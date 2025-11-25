package io.hedges.zoned.core;

import io.hedges.zoned.core.dom.DnsMessageDom;
import io.hedges.zoned.core.dom.Transport;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.net.InetSocketAddress;
import java.time.Instant;

@Getter
@Builder
@ToString
public class DnsRequestContextDom {
    private InetSocketAddress clientAddress;
    private Transport transport;
    private DnsMessageDom query;
    private Instant receivedAt;

}
