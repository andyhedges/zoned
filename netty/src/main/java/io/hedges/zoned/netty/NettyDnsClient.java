package io.hedges.zoned.netty;

import io.hedges.zoned.core.DnsClient;
import io.hedges.zoned.core.dom.DnsMessageDom;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.net.InetSocketAddress;

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
        CompletableFuture<DnsMessageDom> cf = new CompletableFuture<>();
        InetSocketAddress sender = new InetSocketAddress(0);
        EventLoop loop = group.next();
        Bootstrap b = new Bootstrap()
                .group(loop)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, false)
                .localAddress(sender)
                .remoteAddress(upstream)
                .handler(new ChannelInitializer<NioDatagramChannel>() {
                    @Override
                    protected void initChannel(NioDatagramChannel ch) {
                        ch.pipeline().addLast(new DnsDatagramDecoder());
                        ch.pipeline().addLast(new DnsDatagramEncoder());
                        ch.pipeline().addLast(new SimpleChannelInboundHandler<UdpDnsInbound>() {

                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx,
                                                        UdpDnsInbound inbound) {
                                try {
                                    // Only handle the response that matches our query ID
                                    if (inbound.message().header().id() != msg.header().id()) {
                                        return;
                                    }

                                    if (!cf.isDone()) {
                                        cf.complete(inbound.message());
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
            ch.writeAndFlush(new UdpDnsOutbound(msg, upstream)).addListener((ChannelFuture writeFuture) -> {
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
