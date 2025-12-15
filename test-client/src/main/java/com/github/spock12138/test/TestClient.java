package com.github.spock12138.test;

import com.github.spock12138.rpc.api.HelloService;
import com.github.spock12138.rpc.core.proxy.ClientProxy;

// 修改 TestClient.java
public class TestClient {
    public static void main(String[] args) {
        ClientProxy clientProxy = new ClientProxy();
        HelloService helloService = clientProxy.getProxy(HelloService.class);

        // 【修改】循环调用 5 次，看看是不是轮询的
        for (int i = 0; i < 5; i++) {
            String res = helloService.sayHello("Spock-" + i);
            System.out.println(res);

            // 稍微停顿一下，方便观察
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
        }
    }
}