// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core;

import io.hedges.zoned.core.dom.DnsMessageDom;
import io.hedges.zoned.core.dom.Transport;

import java.util.concurrent.CompletionStage;

public interface DnsClient {

    CompletionStage<DnsMessageDom> send(DnsMessageDom message, Transport transport);

    default CompletionStage<DnsMessageDom> send(DnsMessageDom message) {
        return send(message, null);
    }

}
