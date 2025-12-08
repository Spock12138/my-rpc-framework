package com.github.spock12138.test;

import com.github.spock12138.rpc.api.HelloService;

public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
        return "你好, " + name + "! (来自 RPC 服务端)";
    }
}