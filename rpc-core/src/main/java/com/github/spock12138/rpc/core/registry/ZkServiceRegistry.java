package com.github.spock12138.rpc.core.registry;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import java.net.InetSocketAddress;

public class ZkServiceRegistry implements ServiceRegistry {

    private final CuratorFramework client;

    public ZkServiceRegistry() {
        // 1. 重试策略：指数退避策略 (每隔 1s 重试一次，最多 3 次)
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);

        // 2. 创建 ZK 客户端
        this.client = CuratorFrameworkFactory.builder()
                .connectString("127.0.0.1:2181") // 你的 ZK 地址
                .sessionTimeoutMs(40000)         // 会话超时时间
                .retryPolicy(retryPolicy)        // 绑定重试策略
                .namespace("my-rpc")             // 【重要】所有节点都会在这个根目录下
                .build();

        // 3. 启动客户端
        this.client.start();
        System.out.println("Zookeeper 连接成功！");
    }

    @Override
    public void register(String serviceName, InetSocketAddress inetSocketAddress) {
        try {
            // 路径格式：/com.xxx.HelloService/127.0.0.1:9000
            // 因为已经在构造函数里设置了 namespace("my-rpc")，所以实际路径是 /my-rpc/com.xxx...
            String servicePath = "/" + serviceName + "/" + getServiceAddress(inetSocketAddress);

            // 【新增】如果节点已经存在，先删除它（防止之前非正常关闭残留）
            if (client.checkExists().forPath(servicePath) != null) {
                System.out.println("发现旧节点，正在删除: " + servicePath);
                client.delete().forPath(servicePath);
            }

            // 【核心】创建临时节点
            // creatingParentsIfNeeded(): 如果父节点不存在自动创建
            // withMode(CreateMode.EPHEMERAL): 临时节点，断开连接自动删除
            client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL)
                    .forPath(servicePath);

            System.out.println("服务注册成功: " + servicePath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 辅助方法：把地址对象转成字符串 "ip:port"
    private String getServiceAddress(InetSocketAddress serverAddress) {
        return serverAddress.getHostString() + ":" + serverAddress.getPort();
    }
}
