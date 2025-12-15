package com.github.spock12138.rpc.core.loadbalancer;

import java.util.List;

public interface LoadBalancer {
    /**
     * 从地址列表中选择一个
     * @param serviceAddresses 可用的服务地址列表 (如 ["127.0.0.1:9000", "192.168.0.1:9000"])
     * @return 被选中的地址
     */
    String select(List<String> serviceAddresses);
}