package net.hedges.dns;

public final class DnsServerConfig {
    private final int port;
    private final int workerThreads;
    private final boolean reuseAddress;

    public DnsServerConfig(int port, int workerThreads, boolean reuseAddress) {
        this.port = port;
        this.workerThreads = workerThreads;
        this.reuseAddress = reuseAddress;
    }

    public int port() { return port; }
    public int workerThreads() { return workerThreads; }
    public boolean reuseAddress() { return reuseAddress; }

    public static DnsServerConfig defaultConfig() {
        return new DnsServerConfig(53,
                Runtime.getRuntime().availableProcessors(),
                true);
    }
}
