package io.hedges.zoned.netty;

import io.hedges.zoned.core.DnsClient;
import io.hedges.zoned.core.dom.DnsMessageDom;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.dns.*;
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
    private final InetSocketAddress upstream;

    public NettyDnsClient(EventLoopGroup group, InetSocketAddress upstream) {
        this.group = group;
        this.upstream = upstream;
    }

    public CompletionStage<DnsMessageDom> send(DnsMessageDom msg) {
        InetSocketAddress sender = new InetSocketAddress(0);
        DatagramDnsQuery dnsQuery = NettyDnsMapper.toNettyQuery(msg, sender, upstream);
        CompletableFuture<DnsMessageDom> cf = new CompletableFuture<>();
        EventLoop loop = group.next();
        Bootstrap b = new Bootstrap()
                .group(loop)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, false)
                .localAddress(sender)
                .remoteAddress(upstream)
                .handler(new ChannelInitializer<DatagramChannel>() {
                    @Override
                    protected void initChannel(DatagramChannel ch) {
                        ch.pipeline().addLast(new DatagramDnsQueryEncoder());
                        ch.pipeline().addLast(new DatagramDnsResponseDecoder());
                        ch.pipeline().addLast(new SimpleChannelInboundHandler<DatagramDnsResponse>() {

                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx,
                                                        DatagramDnsResponse resp) {
                                try {
                                    // Only handle the response that matches our query ID
                                    if (resp.id() != dnsQuery.id()) {
                                        return;
                                    }

                                    DnsMessageDom dom = NettyDnsMapper.fromNetty(resp);
                                    if (!cf.isDone()) {
                                        cf.complete(dom);
                                    }
                                } catch (Throwable t) {
                                    if (!cf.isDone()) {
                                        cf.completeExceptionally(t);
                                    }
                                } finally {
                                    ctx.close();
                                }
                            }

                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                if (!cf.isDone()) {
                                    cf.completeExceptionally(cause);
                                }
                                ctx.close();
                            }
                        });
                    }
                });

        // Bind and then send the query
        b.bind().addListener((ChannelFuture bindFuture) -> {
            if (!bindFuture.isSuccess()) {
                cf.completeExceptionally(bindFuture.cause());
                return;
            }

            Channel ch = bindFuture.channel();
            ch.writeAndFlush(dnsQuery).addListener((ChannelFuture writeFuture) -> {
                if (!writeFuture.isSuccess()) {
                    if (!cf.isDone()) {
                        cf.completeExceptionally(writeFuture.cause());
                    }
                    ch.close();
                }
            });
        });

        return cf;
    }
}
