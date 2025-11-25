package io.hedges.zoned.netty;

import io.hedges.zoned.core.domain.DnsMessageDom;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;

record PendingRequest(int originalId, CompletableFuture<DnsMessageDom> future, ScheduledFuture<?> timeoutTask) {
}
