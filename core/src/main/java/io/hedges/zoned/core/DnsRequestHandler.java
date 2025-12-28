// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core;

import io.hedges.zoned.core.dom.DnsMessageDom;

import java.util.concurrent.CompletionStage;

public interface DnsRequestHandler {

    public CompletionStage<DnsMessageDom> handle(DnsRequestContext ctx);
}
