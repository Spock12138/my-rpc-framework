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

    public void sendRequest(String host, int port) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            // 添加编解码器 (要和服务端对应)
                            pipeline.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                            pipeline.addLast(new ObjectEncoder());

                            // 替换掉原来的 ChannelInboundHandlerAdapter
                            pipeline.addLast(new SimpleChannelInboundHandler<RpcResponse>() {
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    // 连接建立成功后，发送请求
                                    // 【注意】这里不再是发测试数据，而是发真正符合 HelloService 接口定义的请求
                                    RpcRequest request = RpcRequest.builder()
                                            .interfaceName("com.github.spock12138.rpc.api.HelloService")
                                            .methodName("sayHello")
                                            .parameters(new Object[]{"Spock"})
                                            .paramTypes(new Class[]{String.class})
                                            .build();

                                    ctx.writeAndFlush(request);
                                    System.out.println("客户端已发送消息！");
                                }

                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) throws Exception {
                                    // 【新增】这里就是专门用来接收服务端回信的地方
                                    System.out.println("====== 收到服务端响应 ======");
                                    System.out.println("状态码: " + msg.getCode());
                                    System.out.println("结果: " + msg.getData());
                                    System.out.println("==========================");
                                }

                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                    // 最好加上异常处理，万一断网了能知道
                                    cause.printStackTrace();
                                    ctx.close();
                                }
                            });
                        }
                    });

            ChannelFuture future = bootstrap.connect(host, port).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

    // 临时 main 方法
    public static void main(String[] args) {
        new RpcClient().sendRequest("127.0.0.1", 9000);
    }
}