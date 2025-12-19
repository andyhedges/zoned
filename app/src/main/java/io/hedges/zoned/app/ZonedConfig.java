package io.hedges.zoned.app;

import java.util.List;
import java.util.Map;

public class ZonedConfig {
    private int serverPort = 53;
    private String activeDnsServerPool = "default";
    private Map<String, List<DnsServerConfig>> dnsServerPools;

    public ZonedConfig() {
        DnsServerConfig server = new DnsServerConfig();
        server.setDo53(new Do53TransportConfig("1.1.1.1", 53));
        this.dnsServerPools = Map.of("default", List.of(server));
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getActiveDnsServerPool() {
        return activeDnsServerPool;
    }

    public void setActiveDnsServerPool(String activeDnsServerPool) {
        this.activeDnsServerPool = activeDnsServerPool;
    }

    public Map<String, List<DnsServerConfig>> getDnsServerPools() {
        return dnsServerPools;
    }

    public void setDnsServerPools(Map<String, List<DnsServerConfig>> dnsServerPools) {
        this.dnsServerPools = dnsServerPools;
    }
}
