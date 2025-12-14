package com.github.spock12138.test;

import com.github.spock12138.rpc.api.HelloService;
import com.github.spock12138.rpc.core.proxy.ClientProxy;

public class TestClient {
    public static void main(String[] args) {
        // 1. 创建代理对象 (现在不需要传 IP 和端口了！)
        // 它内部会自动去连 Zookeeper 找地址
        ClientProxy clientProxy = new ClientProxy();

        // 2. 获取服务
        HelloService helloService = clientProxy.getProxy(HelloService.class);

        // 3. 调用
        String res = helloService.sayHello("Spock-ZK");
        System.out.println(res);
    }
}