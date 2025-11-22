package io.hedges.zoned.netty;

import io.hedges.zoned.core.DnsMessageDom;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;

final class PendingRequest {
    final int originalId;
    final CompletableFuture<DnsMessageDom> future;
    final ScheduledFuture<?> timeoutTask;

    PendingRequest(int originalId,
                   CompletableFuture<DnsMessageDom> future,
                   ScheduledFuture<?> timeoutTask) {
        this.originalId = originalId;
        this.future = future;
        this.timeoutTask = timeoutTask;
    }
}
