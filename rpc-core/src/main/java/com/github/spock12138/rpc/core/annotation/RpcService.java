package com.github.spock12138.rpc.core.annotation;

import org.springframework.stereotype.Component;
import java.lang.annotation.*;

@Target(ElementType.TYPE) // 作用在类/接口上
@Retention(RetentionPolicy.RUNTIME) // 运行时可以通过反射获取
@Component // 加上这个，Spring 扫描时会自动把它当成一个 Bean 管理
public @interface RpcService {

    // 服务接口的类对象 (比如 HelloService.class)
    Class<?> interfaceClass();

    // 版本号 (可选，默认 1.0)
    String version() default "1.0";
}