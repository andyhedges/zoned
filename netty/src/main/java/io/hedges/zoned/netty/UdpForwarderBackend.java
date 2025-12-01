package io.hedges.zoned.netty;

import io.hedges.zoned.core.DnsRequestContext;
import io.hedges.zoned.core.dom.DnsMessageDom;
import io.hedges.zoned.core.DnsResolver;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.dns.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.net.InetSocketAddress;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class UdpForwarderBackend implements DnsResolver {

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
         .option(ChannelOption.SO_REUSEADDR, true)
         .handler(new ChannelInitializer<DatagramChannel>() {
             @Override
             protected void initChannel(DatagramChannel ch) {
                 ChannelPipeline p = ch.pipeline();
                 p.addLast("upstreamWireLogger", new LoggingHandler(LogLevel.INFO));
                 p.addLast(new DatagramDnsQueryEncoder());
                 p.addLast(new DatagramDnsResponseDecoder());
                 p.addLast(new UpstreamResponseHandler(pending));
             }
         });

        this.channel = b.bind(0).sync().channel();
    }

    @Override
    public CompletionStage<DnsMessageDom> resolve(DnsRequestContext request) {
//        Channel ch = this.channel;
//        if (ch == null || !ch.isActive()) {
//            CompletableFuture<DnsMessageDom> f = new CompletableFuture<>();
//            f.completeExceptionally(new IllegalStateException("Forwarder not started"));
//            return f;
//        }
//
//        int upstreamId = nextId();
//        CompletableFuture<DnsMessageDom> result = new CompletableFuture<>();
//
//        ScheduledFuture<?> timeoutTask = ch.eventLoop().schedule(() -> {
//            PendingRequest removed = pending.remove(upstreamId);
//            if (removed != null) {
//                removed.future.completeExceptionally(
//                        new TimeoutException("DNS upstream timeout"));
//            }
//        }, timeoutMillis, TimeUnit.MILLISECONDS);
//
//        PendingRequest pr = new PendingRequest(request.getQuery().getId(), result, timeoutTask);
//        pending.put(upstreamId, pr);
//
//        DatagramDnsQuery upstreamQuery = buildUpstreamQuery(ch, upstreamId, request);
//
//        ch.writeAndFlush(upstreamQuery).addListener(future -> {
//            if (!future.isSuccess()) {
//                PendingRequest removed = pending.remove(upstreamId);
//                if (removed != null) {
//                    removed.timeoutTask.cancel(false);
//                    removed.future.completeExceptionally(future.cause());
//                }
//            }
//        });
//
//        return result;
        return null;
    }

    private DatagramDnsQuery buildUpstreamQuery(Channel ch,
                                                int upstreamId,
                                                DnsRequestContext request) {
//        InetSocketAddress local = (InetSocketAddress) ch.localAddress();
//        DatagramDnsQuery q = new DatagramDnsQuery(
//                local,
//                upstream,
//                upstreamId
//        );
//
//        q.setRecursionDesired(request.getQuery().isRecursionDesired());
//
//        List<DnsQuestionDom> questions = request.getQuery().getQuestions();
//        for (DnsQuestionDom domQ : questions) {
//            q.addRecord(DnsSection.QUESTION,
//                    new DefaultDnsQuestion(
//                            domQ.getName().value(),
//                            NettyDnsMapper.mapType(domQ.getType()),
//                            NettyDnsMapper.mapClass(domQ.getRecordClass())
//                    ));
//        }
//
//        return q;
        return null;
    }

    private int nextId() {
        return idGen.updateAndGet(current -> {
            int next = (current + 1) & 0xFFFF;
            return next == 0 ? 1 : next;
        });
    }
}
