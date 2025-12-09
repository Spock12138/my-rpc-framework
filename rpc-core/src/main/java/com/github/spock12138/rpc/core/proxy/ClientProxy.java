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
        RpcRequest request = RpcRequest.builder()
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameters(args)
                .paramTypes(method.getParameterTypes())
                .build();

        RpcClient client = new RpcClient(host, port);

        // 【修改点】直接返回 sendRequest 的结果！
        // 昨天这里是 return null，今天它有值了！
        return client.sendRequest(request);
    }
}