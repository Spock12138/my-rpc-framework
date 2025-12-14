package com.github.spock12138.rpc.core.registry;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.net.InetSocketAddress;
import java.util.List;

public class ZkServiceDiscovery implements ServiceDiscovery {

    private final CuratorFramework client;

    public ZkServiceDiscovery() {
        // 1. 连接 ZK (和 Server 端一模一样的代码，实际开发中可以抽取成工具类)
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        this.client = CuratorFrameworkFactory.builder()
                .connectString("127.0.0.1:2181")
                .sessionTimeoutMs(40000)
                .retryPolicy(retryPolicy)
                .namespace("my-rpc")
                .build();
        this.client.start();
    }

    @Override
    public InetSocketAddress lookupService(String serviceName) {
        try {
            // 1. 获取该服务下所有的子节点 (也就是所有可用的地址列表)
            // 路径: /my-rpc/com.xxx.HelloService
            String servicePath = "/" + serviceName;
            List<String> serviceAddresses = client.getChildren().forPath(servicePath);

            // 2. 判空 (如果列表是空的，说明没人提供这个服务)
            if (serviceAddresses == null || serviceAddresses.isEmpty()) {
                throw new RuntimeException("找不到服务: " + serviceName);
            }

            // 3. 负载均衡 (今天先简单粗暴：直接取第一个)
            // 明天我们会在这里把 get(0) 换成 LoadBalancer.select(list)
            String address = serviceAddresses.get(0);

            // 4. 解析字符串 "127.0.0.1:9000" -> host 和 port
            String[] parts = address.split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);

            return new InetSocketAddress(host, port);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}