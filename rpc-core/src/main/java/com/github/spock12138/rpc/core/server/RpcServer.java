package com.github.spock12138.rpc.core.server;

import com.github.spock12138.rpc.common.entity.RpcRequest;
import com.github.spock12138.rpc.common.entity.RpcResponse;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class RpcServer {

    private final int port;
    // 【新增】本地注册表：存放 接口名 -> 实现类对象 的映射
    private final Map<String, Object> serviceMap = new HashMap<>();

    public RpcServer(int port) {
        this.port = port;
    }

    // 【新增】注册服务的方法
    public void register(String serviceName, Object serviceImpl) {
        serviceMap.put(serviceName, serviceImpl);
    }

    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                            pipeline.addLast(new ObjectEncoder());

                            // 业务 Handler
                            pipeline.addLast(new SimpleChannelInboundHandler<RpcRequest>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) {
                                    // 【核心修改】这里不再只是打印，而是去执行！
                                    RpcResponse<Object> response = new RpcResponse<>();
                                    try {
                                        // 1. 从注册表中拿到实现类对象
                                        Object serviceImpl = serviceMap.get(msg.getInterfaceName());

                                        // 2. 利用反射找到方法
                                        Method method = serviceImpl.getClass().getMethod(
                                                msg.getMethodName(),
                                                msg.getParamTypes()
                                        );

                                        // 3. 执行方法
                                        Object result = method.invoke(serviceImpl, msg.getParameters());

                                        // 4. 封装成功结果
                                        response.setCode(200);
                                        response.setData(result);

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        response.setCode(500);
                                        response.setMessage(e.getMessage());
                                    }

                                    // 5. 【关键】把结果写回给客户端
                                    ctx.writeAndFlush(response);
                                }
                            });
                        }
                    })
                    // ...省略 option 配置...
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture future = serverBootstrap.bind(port).sync();
            System.out.println("RPC 服务端启动成功，监听端口: " + port);
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}