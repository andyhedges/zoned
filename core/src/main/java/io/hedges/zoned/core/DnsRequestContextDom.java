package io.hedges.zoned.core;

import io.hedges.zoned.core.dom.DnsMessageDom;
import io.hedges.zoned.core.dom.Transport;

import java.net.InetSocketAddress;
import java.time.Instant;

public class DnsRequestContextDom {
    private final InetSocketAddress clientAddress;
    private final Transport transport;
    private final DnsMessageDom query;
    private final Instant receivedAt;

    public DnsRequestContextDom(InetSocketAddress clientAddress,
                                Transport transport,
                                DnsMessageDom query,
                                Instant receivedAt) {
        this.clientAddress = clientAddress;
        this.transport = transport;
        this.query = query;
        this.receivedAt = receivedAt;
    }

    public InetSocketAddress getClientAddress() {
        return clientAddress;
    }

    public Transport getTransport() {
        return transport;
    }

    public DnsMessageDom getQuery() {
        return query;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }
}
