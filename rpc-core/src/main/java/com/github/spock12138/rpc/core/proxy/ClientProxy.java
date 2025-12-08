package com.github.spock12138.rpc.core.proxy;

import com.github.spock12138.rpc.common.entity.RpcRequest;
import com.github.spock12138.rpc.core.client.RpcClient;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ClientProxy implements InvocationHandler {

    private final String host;
    private final int port;

    public ClientProxy(String host, int port) {
        this.host = host;
        this.port = port;
    }

    //这是对外提供的方法，用来生成代理对象
    // 泛型 T 表示生成的代理对象类型（比如 HelloService）
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        // JDK 动态代理的固定写法 (类加载器, 接口列表, 代理处理逻辑)
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class<?>[]{clazz},
                this
        );
    }

    // 【核心逻辑】当用户调用 helloService.sayHello() 时，会被这里拦截！
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 1. 拦截方法调用，构建 RpcRequest 对象
        RpcRequest request = RpcRequest.builder()
                .interfaceName(method.getDeclaringClass().getName()) // 接口名
                .methodName(method.getName())                        // 方法名
                .parameters(args)                                    // 参数
                .paramTypes(method.getParameterTypes())              // 参数类型
                .build();

        // 2. 创建 RpcClient，把信发出去
        RpcClient client = new RpcClient(host, port);

        // 3. 发送！
        client.sendRequest(request);

        // 目前先返回 null，因为 RpcClient 还没法同步返回结果
        return null;
    }
}