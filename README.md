# My RPC Framework

一个基于 Netty + Zookeeper + Java 实现的轻量级 RPC 框架。
（这是我研二期间为了深入理解分布式网络通信而手写的轮子）

## 🚀 已实现功能
- [x] 基于 Netty 的 NIO 通信
- [x] 自定义通信协议 (RpcRequest/RpcResponse)
- [x] Client 端动态代理 (屏蔽网络细节)
- [ ] Zookeeper 服务注册与发现 (开发中...)
- [ ] 负载均衡策略 (开发中...)

## 🛠 技术栈
- Java 17
- Netty 4.1
- Lombok