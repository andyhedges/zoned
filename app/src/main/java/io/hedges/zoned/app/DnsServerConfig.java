// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.app;

public class DnsServerConfig {
    private UdpTransportConfig udp;
    private TcpTransportConfig tcp;
    private DohTransportConfig doh;
    private DoqTransportConfig doq;
    private Do53TransportConfig do53;

    public DnsServerConfig() {
    }

    public DnsServerConfig(UdpTransportConfig udp) {
        this.udp = udp;
    }

    public UdpTransportConfig getUdp() {
        return udp;
    }

    public void setUdp(UdpTransportConfig udp) {
        this.udp = udp;
    }

    public TcpTransportConfig getTcp() {
        return tcp;
    }

    public void setTcp(TcpTransportConfig tcp) {
        this.tcp = tcp;
    }

    public DohTransportConfig getDoh() {
        return doh;
    }

    public void setDoh(DohTransportConfig doh) {
        this.doh = doh;
    }

    public DoqTransportConfig getDoq() {
        return doq;
    }

    public void setDoq(DoqTransportConfig doq) {
        this.doq = doq;
    }

    public Do53TransportConfig getDo53() {
        return do53;
    }

    public void setDo53(Do53TransportConfig do53) {
        this.do53 = do53;
    }
}
