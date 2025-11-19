package net.hedges.dns;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;

public final class PendingRequest {
    final int originalId;
    final CompletableFuture<DnsResponseEnvelope> future;
    final ScheduledFuture<?> timeoutTask;

    PendingRequest(int originalId,
                   CompletableFuture<DnsResponseEnvelope> future,
                   ScheduledFuture<?> timeoutTask) {
        this.originalId = originalId;
        this.future = future;
        this.timeoutTask = timeoutTask;
    }
}

