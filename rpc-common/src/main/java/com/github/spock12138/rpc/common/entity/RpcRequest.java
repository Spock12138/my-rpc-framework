package com.github.spock12138.rpc.common.entity; // 这一行必须是你刚才建的包名

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 客户端请求实体类
 * 传输时需要序列化，所以必须实现 Serializable 接口
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RpcRequest implements Serializable {
    private String requestId; // 【新增】唯一的请求 ID
    private String interfaceName;
    private String methodName;
    private Object[] parameters;
    private Class<?>[] paramTypes;
}