package io.hedges.zoned.core;

import io.hedges.zoned.core.dom.DnsMessageDom;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CompletionStage;

public interface DnsClient {

    public CompletionStage<DnsMessageDom> send(DnsMessageDom message);

}
