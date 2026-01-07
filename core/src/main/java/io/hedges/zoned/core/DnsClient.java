// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core;

import io.hedges.zoned.core.dom.DnsMessageDom;
import io.hedges.zoned.core.dom.Transport;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletionStage;

public interface DnsClient {

    CompletionStage<DnsMessageDom> send(DnsMessageDom message, InetSocketAddress server, Transport transport);

    default CompletionStage<DnsMessageDom> send(DnsMessageDom message) {
        return send(message, null, null);
    }

    default CompletionStage<DnsMessageDom> send(DnsMessageDom message, Transport transport) {
        return send(message, null, transport);
    }

    default CompletionStage<DnsMessageDom> send(DnsMessageDom message, InetSocketAddress server) {
        return send(message, server, null);
    }
}
