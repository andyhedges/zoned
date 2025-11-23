package io.hedges.zoned.core.domain;

import java.net.InetSocketAddress;
import java.time.Instant;

public final class DnsRequestContextDom {
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
