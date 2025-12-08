package com.github.spock12138.test;

import com.github.spock12138.rpc.api.HelloService;
import com.github.spock12138.rpc.core.server.RpcServer;

public class TestServer {
    public static void main(String[] args) {
        // 1. 创建真正的业务对象
        HelloService helloService = new HelloServiceImpl();

        // 2. 启动 RPC 服务端，监听 9000 端口
        RpcServer server = new RpcServer(9000);

        // 3. 注册服务：告诉服务端，如果有叫 "HelloService" 的请求，就用 helloService 这个对象来处理
        server.register(HelloService.class.getName(), helloService);

        // 4. 正式启动
        server.start();
    }
}