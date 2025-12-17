package com.github.spock12138.rpc.core.annotation;

import java.lang.annotation.*;

@Target(ElementType.FIELD) // 作用在字段上
@Retention(RetentionPolicy.RUNTIME) // 运行时可以通过反射获取
public @interface RpcReference {

    // 版本号 (可选，默认 1.0)
    String version() default "1.0";
}