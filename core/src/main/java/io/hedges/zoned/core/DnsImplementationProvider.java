package io.hedges.zoned.core;

public interface DnsImplementationProvider {

    public DnsServer server();

    public DnsClient client();

}
