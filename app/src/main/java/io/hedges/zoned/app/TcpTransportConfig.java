// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.app;

public class TcpTransportConfig {
    private String host;
    private int port = 53;

    public TcpTransportConfig() {
    }

    public TcpTransportConfig(String host, int port) {
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
