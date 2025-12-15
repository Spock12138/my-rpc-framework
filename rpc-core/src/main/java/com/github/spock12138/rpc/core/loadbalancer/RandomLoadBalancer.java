package com.github.spock12138.rpc.core.loadbalancer;

import java.util.List;
import java.util.Random;

public class RandomLoadBalancer implements LoadBalancer {

    private final Random random = new Random();

    @Override
    public String select(List<String> serviceAddresses) {
        // 1. 判空防卫 (虽然 ZkServiceDiscovery 那边判过了，但作为工具类最好健壮一点)
        if (serviceAddresses == null || serviceAddresses.isEmpty()) {
            return null;
        }

        // 2. 如果只有一个，不用随机了，直接返回
        if (serviceAddresses.size() == 1) {
            return serviceAddresses.get(0);
        }

        // 3. 生成一个 [0, size) 的随机数
        int index = random.nextInt(serviceAddresses.size());

        // 4. 返回对应位置的地址
        return serviceAddresses.get(index);
    }
}