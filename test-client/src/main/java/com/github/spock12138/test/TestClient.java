package com.github.spock12138.test;

import com.github.spock12138.rpc.api.HelloService;
import com.github.spock12138.rpc.core.proxy.ClientProxy;

public class TestClient {
    public static void main(String[] args) {
        // 1. 创建代理工厂 (经纪人)
        ClientProxy clientProxy = new ClientProxy("127.0.0.1", 9000);

        // 2. 获取 HelloService 的代理对象
        // 这一步之后，helloService 表面上是 HelloService 接口，实际上是 ClientProxy 生成的代理
        HelloService helloService = clientProxy.getProxy(HelloService.class);

        // 3. 像调用本地方法一样调用！
        // 这一行代码执行时，会自动触发 ClientProxy 的 invoke 方法 -> 构建 Request -> 发给 Netty
        String res = helloService.sayHello("Spock-Proxy");
        System.out.println(res);
    }
}