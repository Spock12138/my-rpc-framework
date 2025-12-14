package com.github.spock12138.rpc.core.registry;

import java.net.InetSocketAddress;

public interface ServiceRegistry {
    /**
     * 注册服务
     * @param serviceName 服务名称 (如 com.xxx.HelloService)
     * @param inetSocketAddress 服务地址 (如 127.0.0.1:9000)
     */
    void register(String serviceName, InetSocketAddress inetSocketAddress);

    // 今天先不写发现服务 (lookup)，贪多嚼不烂，先把注册写好
}