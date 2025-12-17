package com.github.spock12138.test;

import com.github.spock12138.rpc.api.HelloService;
import com.github.spock12138.rpc.core.annotation.RpcService;

// 【关键】贴上便利贴，告诉 Spring 这是一个 RPC 服务
@RpcService(interfaceClass = HelloService.class)
public class HelloServiceImpl implements HelloService {

    @Override
    public String sayHello(String name) {
        return "你好, " + name + "! (来自 Spring 版 RPC)";
    }
}