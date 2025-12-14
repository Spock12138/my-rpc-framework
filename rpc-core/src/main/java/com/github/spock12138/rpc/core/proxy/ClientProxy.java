package com.github.spock12138.rpc.core.proxy;

import com.github.spock12138.rpc.common.entity.RpcRequest;
import com.github.spock12138.rpc.core.client.RpcClient;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.github.spock12138.rpc.core.registry.ServiceDiscovery;
import com.github.spock12138.rpc.core.registry.ZkServiceDiscovery;
import java.net.InetSocketAddress;

public class ClientProxy implements InvocationHandler {

    // 【修改】不再存死 IP，而是存“发现服务的能力”
    private final ServiceDiscovery serviceDiscovery;

    public ClientProxy() {
        this.serviceDiscovery = new ZkServiceDiscovery();
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

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 1. 构建请求 (不变)
        RpcRequest request = RpcRequest.builder()
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameters(args)
                .paramTypes(method.getParameterTypes())
                .build();

        // 2. 【核心修改】去 Zookeeper 查：这个接口也就谁在做？
        String serviceName = method.getDeclaringClass().getName();
        InetSocketAddress address = serviceDiscovery.lookupService(serviceName);

        // 3. 【核心修改】拿到动态地址后，再连接服务器
        // 注意：address.getHostString() 获取 IP
        RpcClient client = new RpcClient(address.getHostString(), address.getPort());

        // 4. 发送请求 (不变)
        return client.sendRequest(request);
    }
}