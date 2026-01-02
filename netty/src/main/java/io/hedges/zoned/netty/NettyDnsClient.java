// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.netty;

import io.hedges.zoned.core.DnsClient;
import io.hedges.zoned.core.dom.DnsMessageDom;
import io.hedges.zoned.core.dom.Transport;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.socket.SocketChannel;

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

    @Override
    public CompletionStage<DnsMessageDom> send(DnsMessageDom msg) {
        return send(msg, null);
    }

    @Override
    public CompletionStage<DnsMessageDom> send(DnsMessageDom msg, Transport transport) {
        if (transport == Transport.TCP) {
            return sendTcp(msg);
        }
        if (transport == Transport.UDP) {
            return sendUdp(msg);
        }
        return sendWithRetry(msg);
    }

    private CompletionStage<DnsMessageDom> sendWithRetry(DnsMessageDom msg) {
        CompletableFuture<DnsMessageDom> cf = new CompletableFuture<>();
        sendUdp(msg).whenComplete((response, t) -> {
            if (t != null) {
                cf.completeExceptionally(t);
                return;
            }
            if (response == null) {
                cf.complete(null);
                return;
            }
            if (response.header() != null && response.header().truncation()) {
                sendTcp(msg).whenComplete((tcpResponse, tcpError) -> {
                    if (tcpError != null) {
                        cf.complete(response);
                        return;
                    }
                    cf.complete(tcpResponse);
                });
                return;
            }
            cf.complete(response);
        });
        return cf;
    }

    private CompletionStage<DnsMessageDom> sendUdp(DnsMessageDom msg) {
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

    private CompletionStage<DnsMessageDom> sendTcp(DnsMessageDom msg) {
        CompletableFuture<DnsMessageDom> cf = new CompletableFuture<>();
        EventLoop loop = group.next();
        Bootstrap b = new Bootstrap()
                .group(loop)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .remoteAddress(upstream)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new DnsTcpDecoder());
                        ch.pipeline().addLast(new DnsTcpEncoder());
                        ch.pipeline().addLast(new SimpleChannelInboundHandler<DnsMessageDom>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, DnsMessageDom response) {
                                try {
                                    if (response.header().id() != msg.header().id()) {
                                        return;
                                    }
                                    if (!cf.isDone()) {
                                        cf.complete(response);
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

        b.connect().addListener((ChannelFuture connectFuture) -> {
            if (!connectFuture.isSuccess()) {
                cf.completeExceptionally(connectFuture.cause());
                return;
            }

            Channel ch = connectFuture.channel();
            ch.writeAndFlush(msg).addListener((ChannelFuture writeFuture) -> {
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
