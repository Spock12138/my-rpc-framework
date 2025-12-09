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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

public class RpcClient {

    private final String host;
    private final int port;

    // 【新增】全局待处理请求池。Key=RequestId, Value=Future
    private static final Map<String, CompletableFuture<RpcResponse>> PENDING_REQUESTS = new ConcurrentHashMap<>();

    public RpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public Object sendRequest(RpcRequest request) {
        // 1. 【生成 ID】给请求贴上唯一标签
        request.setRequestId(UUID.randomUUID().toString());

        // 2. 【准备支票】创建一个空的 Future，等着 Netty 给我们填结果
        CompletableFuture<RpcResponse> resultFuture = new CompletableFuture<>();

        // 3. 【登记】把 ID 和 Future 存起来，表示“我在等这个 ID 的结果”
        PENDING_REQUESTS.put(request.getRequestId(), resultFuture);

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

                            // 业务 Handler
                            pipeline.addLast(new SimpleChannelInboundHandler<RpcResponse>() {
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) {
                                    // 连接建立，发送请求
                                    ctx.writeAndFlush(request);
                                }

                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) {
                                    // 【核心】收到回信了！
                                    // 1. 看看回信的 ID 是多少
                                    String requestId = msg.getRequestId();

                                    // 2. 去“待处理池”里找到对应的 Future
                                    CompletableFuture<RpcResponse> future = PENDING_REQUESTS.remove(requestId);

                                    // 3. 如果找到了，把结果填进去！(这会唤醒主线程)
                                    if (future != null) {
                                        future.complete(msg);
                                    }
                                }
                            });
                        }
                    });

            ChannelFuture future = bootstrap.connect(host, port).sync();

            // 4. 【同步等待】主线程卡在这里，死等 resultFuture 被填入数据
            // get() 方法会阻塞，直到上面的 complete() 被调用
            RpcResponse response = resultFuture.get();

            // 5. 返回真正的数据
            return response.getData();

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        } finally {
            group.shutdownGracefully();
        }
    }
}