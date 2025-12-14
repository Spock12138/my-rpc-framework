package com.github.spock12138.rpc.core.registry;

import java.net.InetSocketAddress;

public interface ServiceDiscovery {
    /**
     * 根据服务名称查找服务地址
     * @param serviceName 服务名称 (如 com.xxx.HelloService)
     * @return 服务地址
     */
    InetSocketAddress lookupService(String serviceName);
}