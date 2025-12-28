// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core;

import io.hedges.zoned.core.dom.DnsMessageDom;
import io.hedges.zoned.core.dom.Transport;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.Map;

@Getter
@Builder
@ToString
public class DnsRequestContext {
    private InetSocketAddress clientAddress;
    private Transport transport;
    private DnsMessageDom query;
    private Instant receivedAt;
    private Map<String, Object> data;
    @Setter
    private DnsMessageDom response;
}
