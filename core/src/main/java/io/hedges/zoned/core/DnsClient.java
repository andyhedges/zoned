package io.hedges.zoned.core;

import io.hedges.zoned.core.dom.DnsMessageDom;

import java.util.concurrent.CompletionStage;

public interface DnsClient {

    public CompletionStage<DnsMessageDom> send(DnsMessageDom message);

}
