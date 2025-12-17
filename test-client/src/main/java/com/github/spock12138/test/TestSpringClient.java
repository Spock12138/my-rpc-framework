package com.github.spock12138.test;

import com.github.spock12138.rpc.api.HelloService;
import com.github.spock12138.rpc.core.annotation.RpcReference;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * 客户端测试：验证 @RpcReference 自动注入
 */
@Configuration
@ComponentScan("com.github.spock12138") // 扫描核心包和当前包
public class TestSpringClient {

    public static void main(String[] args) {
        // 1. 启动 Spring 容器
        ApplicationContext context = new AnnotationConfigApplicationContext(TestSpringClient.class);

        // 2. 从容器中拿出 HelloController Bean
        HelloController controller = context.getBean(HelloController.class);

        // 3. 调用方法，见证奇迹
        controller.test();
    }
}

// 模拟一个 Controller，只有加了 @Component 才能被 Spring 管理
@Component
class HelloController {

    // 【关键验证点】这里没有 new，也没有 getProxy，全靠 Spring 自动注入！
    @RpcReference
    private HelloService helloService;

    public void test() {
        System.out.println("----------------------------------------");
        System.out.println("开始调用远程服务...");
        // 如果自动注入成功，这里就不会报空指针异常
        String result = helloService.sayHello("Spring RPC");
        System.out.println("调用结果: " + result);
        System.out.println("----------------------------------------");
    }
}