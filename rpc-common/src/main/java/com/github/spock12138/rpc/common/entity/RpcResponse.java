package com.github.spock12138.rpc.common.entity;

import lombok.Data;
import java.io.Serializable;

/**
 * 服务端响应实体类
 * T 是泛型，用来装任何类型的返回结果
 */
@Data
public class RpcResponse<T> implements Serializable {
    private Integer code; // 状态码: 200 成功
    private String message; // 错误信息
    private T data;       // 返回的数据

    // 写两个静态方法，方便快速生成成功或失败的响应
    public static <T> RpcResponse<T> success(T data) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(200);
        response.setData(data);
        return response;
    }

    public static <T> RpcResponse<T> fail(String message) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(500);
        response.setMessage(message);
        return response;
    }
}