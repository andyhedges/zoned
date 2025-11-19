package net.hedges.dns;

import io.netty.handler.codec.dns.DnsMessage;
import io.netty.handler.codec.dns.DnsQuestion;
import lombok.Data;

import java.net.SocketAddress;
import java.time.Instant;

@Data
public final class DnsRequestContext {
        private final SocketAddress remoteAddress;
        private final DnsQuestion question;
        private final DnsMessage rawMessage;
        private final Instant receivedAt;
        // you can add tags, matched view, etc
}
