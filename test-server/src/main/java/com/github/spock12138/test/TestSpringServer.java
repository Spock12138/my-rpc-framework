package com.github.spock12138.test;

import com.github.spock12138.rpc.core.annotation.RpcService;
import com.github.spock12138.rpc.core.server.RpcServer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
// 【关键】扫描范围要大，既要扫 rpc-core (拿处理器)，又要扫当前包 (拿服务实现)
// 所以直接扫根包 "com.github.spock12138"
@ComponentScan("com.github.spock12138")
public class TestSpringServer {

    public static void main(String[] args) {
        // 1. 启动 Spring 容器
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestSpringServer.class);

        // 2. 【桥梁】从 Spring 容器中拿出所有的服务 Bean，准备交给 Netty
        Map<String, Object> serviceMap = new HashMap<>();

        // 获取所有贴了 @RpcService 注解的 Bean
        Map<String, Object> annotatedBeans = context.getBeansWithAnnotation(RpcService.class);

        for (Object bean : annotatedBeans.values()) {
            RpcService annotation = bean.getClass().getAnnotation(RpcService.class);
            String interfaceName = annotation.interfaceClass().getName();
            serviceMap.put(interfaceName, bean);
        }

        System.out.println("Spring 初始化完毕，准备启动 Netty Server...");

        // 3. 启动 Netty 服务器
        // 这里把从 Spring 拿到的 serviceMap 传进去，这样 Netty 才能找到实现类
        new RpcServer(9000, serviceMap).start();
    }
}