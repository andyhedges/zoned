package io.hedges.zoned.app;

public class Do53TransportConfig {
    private String host;
    private int port = 53;

    public Do53TransportConfig() {
    }

    public Do53TransportConfig(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
