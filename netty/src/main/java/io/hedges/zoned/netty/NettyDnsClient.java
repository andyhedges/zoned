package io.hedges.zoned.netty;

import io.hedges.zoned.core.DnsClient;
import io.hedges.zoned.core.dom.DnsMessageDom;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.resolver.dns.DnsNameResolver;
import io.netty.resolver.dns.DnsNameResolverBuilder;
import io.netty.resolver.dns.SingletonDnsServerAddressStreamProvider;
import io.netty.util.concurrent.Future;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;


public class NettyDnsClient implements DnsClient {

    private final EventLoopGroup group;
    private final Map<EventLoop, DnsNameResolver> resolvers = new HashMap<>();

    public NettyDnsClient(EventLoopGroup group, InetSocketAddress upstream) {
        this.group = group;
        group.forEach(loop -> resolvers.put(
                (EventLoop) loop,
                new DnsNameResolverBuilder((EventLoop) loop)
                        .datagramChannelType(NioDatagramChannel.class)
                        .nameServerProvider(new SingletonDnsServerAddressStreamProvider(upstream))
                        .optResourceEnabled(true)
                        .recursionDesired(true)
                        .build()
        ));
    }

    public CompletionStage<List<InetAddress>> send(DnsMessageDom msg) {
        Future<List<InetAddress>> f = resolvers.get(group.next()).resolveAll("hedges.net");
        CompletableFuture<List<InetAddress>> cf = new CompletableFuture<>();

        f.addListener((Future<List<InetAddress>> fut) -> {
            if (fut.isSuccess()) {
                System.out.println(fut.getNow());
                cf.complete(fut.getNow());
            } else {
                cf.completeExceptionally(fut.cause());
            }
        });
        return cf;
    }
}
