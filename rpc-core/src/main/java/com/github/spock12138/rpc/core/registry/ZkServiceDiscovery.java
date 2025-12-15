package com.github.spock12138.rpc.core.registry;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.net.InetSocketAddress;
import java.util.List;

import com.github.spock12138.rpc.core.loadbalancer.LoadBalancer;
import com.github.spock12138.rpc.core.loadbalancer.RandomLoadBalancer;
import com.github.spock12138.rpc.core.loadbalancer.RoundRobinLoadBalancer;

public class ZkServiceDiscovery implements ServiceDiscovery {

    private final CuratorFramework client;
    private final LoadBalancer loadBalancer; // 新增

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
//        // 【新增】初始化负载均衡器
//        this.loadBalancer = new RandomLoadBalancer();
        // 【修改】将 RandomLoadBalancer 替换为 RoundRobinLoadBalancer
        this.loadBalancer = new RoundRobinLoadBalancer();
    }

    @Override
    public InetSocketAddress lookupService(String serviceName) {
        try {
            // 1. 获取列表 (代码不变)
            String servicePath = "/" + serviceName;
            List<String> serviceAddresses = client.getChildren().forPath(servicePath);

            if (serviceAddresses == null || serviceAddresses.isEmpty()) {
                throw new RuntimeException("找不到服务: " + serviceName);
            }

            // 2. 【核心修改】负载均衡选择
            // 以前：String address = serviceAddresses.get(0);
            // 现在：
            String address = loadBalancer.select(serviceAddresses);

            System.out.println("负载均衡选择了: " + address); // 打印一下方便观察

            // 3. 解析地址 (代码不变)
            String[] parts = address.split(":");
            return new InetSocketAddress(parts[0], Integer.parseInt(parts[1]));

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}