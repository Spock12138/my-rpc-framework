package com.github.spock12138.rpc.core.spring;

import com.github.spock12138.rpc.core.annotation.RpcReference;
import com.github.spock12138.rpc.core.annotation.RpcService;
import com.github.spock12138.rpc.core.proxy.ClientProxy;
import com.github.spock12138.rpc.core.registry.ServiceRegistry;
import com.github.spock12138.rpc.core.registry.ZkServiceRegistry;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * Spring 的后置处理器
 * 作用：在 Spring Bean 初始化之后，扫描注解，执行“注册服务”和“注入代理”
 */
@Component // 加上这个，Spring 才会加载这个类
public class SpringBeanPostProcessor implements BeanPostProcessor {

    private ServiceRegistry serviceRegistry;

    public SpringBeanPostProcessor() {
        // 这里先暂时硬编码连接 Zookeeper，后面可以用配置文件优化
        this.serviceRegistry = new ZkServiceRegistry("127.0.0.1:2181");
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 1. 【服务端逻辑】检查类头上有没有 @RpcService
        if (bean.getClass().isAnnotationPresent(RpcService.class)) {
            RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
            // 拿到注解上的 interfaceClass (比如 HelloService.class)
            Class<?> interfaceClass = rpcService.interfaceClass();

            // 注册到 Zookeeper (这里假设 Server 跑在 9000 端口，暂时写死，后续优化)
            // 注意：真实的 Server 端口应该从配置文件读取
            serviceRegistry.register(interfaceClass.getName(), new java.net.InetSocketAddress("127.0.0.1", 9000));
            System.out.println("服务端自动注册服务: " + interfaceClass.getName());
        }

        // 2. 【客户端逻辑】检查字段上有没有 @RpcReference
        Field[] declaredFields = bean.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(RpcReference.class)) {
                RpcReference rpcReference = field.getAnnotation(RpcReference.class);

                // 生成代理对象
                ClientProxy clientProxy = new ClientProxy(); // 这里的 IP 端口其实不重要，因为会去 ZK 查
                // 注意：这里需要根据你之前的 ClientProxy 代码调整，看怎么获取代理
                // 假设你有一个 getProxy 方法
                Object proxy = clientProxy.getProxy(field.getType());

                // 暴力注入 (因为字段通常是 private 的)
                field.setAccessible(true);
                try {
                    field.set(bean, proxy);
                    System.out.println("客户端自动注入代理: " + field.getName());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        return bean;
    }
}