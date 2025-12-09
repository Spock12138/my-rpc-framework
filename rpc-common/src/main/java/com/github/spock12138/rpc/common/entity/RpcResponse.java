package com.github.spock12138.rpc.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@Builder  // 【新增】启用 Builder 模式
@NoArgsConstructor  // 【新增】无参构造（反序列化需要）
@AllArgsConstructor // 【新增】全参构造（Builder需要）
public class RpcResponse<T> implements Serializable {

    private String requestId; // 响应对应的请求 ID
    private Integer code;
    private String message;
    private T data;

    // 【可选】你可以保留这两个方法方便快速返回，但不需要传 requestId
    // requestId 由 RpcServer 这一层单独填进去
    public static <T> RpcResponse<T> success(T data) {
        return RpcResponse.<T>builder()
                .code(200)
                .data(data)
                .build();
    }

    public static <T> RpcResponse<T> fail(String message) {
        return RpcResponse.<T>builder()
                .code(500)
                .message(message)
                .build();
    }
}