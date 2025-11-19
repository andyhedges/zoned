package net.hedges.dns;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.dns.*;

import java.net.InetSocketAddress;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class UdpForwarderBackend implements DnsBackend {

    private final EventLoopGroup group;
    private final InetSocketAddress upstream;
    private final long timeoutMillis;

    private final AtomicInteger idGen = new AtomicInteger(1);
    private final ConcurrentMap<Integer, PendingRequest> pending = new ConcurrentHashMap<>();

    private volatile Channel channel;

    public UdpForwarderBackend(EventLoopGroup group,
                               InetSocketAddress upstream,
                               long timeoutMillis) {
        this.group = group;
        this.upstream = upstream;
        this.timeoutMillis = timeoutMillis;
    }

    public void start() throws InterruptedException {
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioDatagramChannel.class)
                .handler(new ChannelInitializer<DatagramChannel>() {
                    @Override
                    protected void initChannel(DatagramChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new DatagramDnsQueryEncoder());
                        p.addLast(new DatagramDnsResponseDecoder());
                        p.addLast(new UpstreamResponseHandler(pending));
                    }
                });

        // bind to any local port, not strictly necessary to connect
        this.channel = b.bind(0).sync().channel();
    }

    @Override
    public CompletionStage<DnsResponseEnvelope> resolve(DnsRequestContext ctx) {
        Channel ch = this.channel;
        if (ch == null || !ch.isActive()) {
            CompletableFuture<DnsResponseEnvelope> f = new CompletableFuture<>();
            f.completeExceptionally(new IllegalStateException("Forwarder not started"));
            return f;
        }

        DatagramDnsQuery original = (DatagramDnsQuery) ctx.getRawMessage();

        int upstreamId = nextId();
        CompletableFuture<DnsResponseEnvelope> result = new CompletableFuture<>();

        ScheduledFuture<?> timeoutTask = ch.eventLoop().schedule(() -> {
            PendingRequest removed = pending.remove(upstreamId);
            if (removed != null) {
                removed.future.completeExceptionally(
                        new TimeoutException("DNS upstream timeout"));
            }
        }, timeoutMillis, TimeUnit.MILLISECONDS);

        PendingRequest pr = new PendingRequest(original.id(), result, timeoutTask);
        pending.put(upstreamId, pr);

        // Build upstream query
        DatagramDnsQuery upstreamQuery = new DatagramDnsQuery(
                (InetSocketAddress)ch.localAddress(),
                upstream,
                upstreamId
        );

        DnsQuestion q = ctx.getQuestion();
        upstreamQuery.addRecord(DnsSection.QUESTION, q);

        ch.writeAndFlush(upstreamQuery).addListener(future -> {
            if (!future.isSuccess()) {
                PendingRequest removed = pending.remove(upstreamId);
                if (removed != null) {
                    removed.timeoutTask.cancel(false);
                    removed.future.completeExceptionally(future.cause());
                }
            }
        });

        return result;
    }

    private int nextId() {
        // simple wrap into 16 bit range
        return idGen.updateAndGet(current -> {
            int next = (current + 1) & 0xFFFF;
            return next == 0 ? 1 : next;
        });
    }
}

