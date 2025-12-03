package io.hedges.zoned.core;

public interface DnsServer {
    DnsServer requestHandler(DnsRequestHandler handler);
    void start() throws Exception;
    void stop();
}
