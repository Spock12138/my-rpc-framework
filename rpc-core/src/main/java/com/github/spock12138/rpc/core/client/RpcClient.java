package com.github.spock12138.rpc.core.client;

import com.github.spock12138.rpc.common.entity.RpcRequest;
import com.github.spock12138.rpc.common.entity.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class RpcClient {

    private final String host;
    private final int port;

    public RpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    // 【修改点】这个方法现在接收一个 RpcRequest 参数
    public Object sendRequest(RpcRequest request) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                            pipeline.addLast(new ObjectEncoder());
                            // 简单的 Handler，只负责发和收
                            pipeline.addLast(new SimpleChannelInboundHandler<RpcResponse>() {
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) {
                                    // 【核心】发送传入的 request，而不是自己造
                                    ctx.writeAndFlush(request);
                                }

                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) {
                                    // 暂时还是打印，后面我们会把这个结果 return 出去
                                    System.out.println("收到响应: " + msg.getData());
                                }
                            });
                        }
                    });

            ChannelFuture future = bootstrap.connect(host, port).sync();
            future.channel().closeFuture().sync();

            // TODO: 这里目前返回 null，因为 Netty 是异步的，拿到返回值需要一点高级技巧（CompletableFuture）
            // 我们明天再解决“怎么拿到返回值”的问题，今天先保证“能发出去”
            return null;

        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } finally {
            group.shutdownGracefully();
        }
    }
}