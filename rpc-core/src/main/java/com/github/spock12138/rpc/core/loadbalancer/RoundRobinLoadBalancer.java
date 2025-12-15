package com.github.spock12138.rpc.core.loadbalancer;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinLoadBalancer implements LoadBalancer {

    // 1. 线程安全的计数器，初始值为 0
    private final AtomicInteger index = new AtomicInteger(0);

    @Override
    public String select(List<String> serviceAddresses) {
        // 判空防卫
        if (serviceAddresses == null || serviceAddresses.isEmpty()) {
            return null;
        }

        // 如果只有一个服务，直接返回，不用浪费计算资源
        if (serviceAddresses.size() == 1) {
            return serviceAddresses.get(0);
        }

        // 2. 核心算法：取模
        // getAndIncrement() 相当于 i++，但它是原子操作
        int current = index.getAndIncrement();

        // 使用 % 取余数，保证结果永远在 [0, size-1] 之间
        // Math.abs 是为了防止 current 溢出变成负数（虽然很久才会溢出一次，但要严谨）
        int position = Math.abs(current % serviceAddresses.size());

        return serviceAddresses.get(position);
    }
}