package com.github.spock12138.rpc.core.codec;

import com.github.spock12138.rpc.core.serializer.Serializer;
import com.github.spock12138.rpc.common.entity.RpcRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 通用编码器
 * 协议格式：
 * +---------------+---------------+-----------------+-------------+
 * |  Magic Number |  Package Type | Serializer Code | Data Length |
 * |    4 bytes    |    4 bytes    |     4 bytes     |   4 bytes   |
 * +---------------+---------------+-----------------+-------------+
 * |                          Data Bytes                           |
 * |                   Length: ${Data Length}                      |
 * +---------------------------------------------------------------+
 */
public class CommonEncoder extends MessageToByteEncoder {

    private static final int MAGIC_NUMBER = 0xCAFEBABE; // 魔数，随便定义，比如咖啡宝贝
    private final Serializer serializer;

    public CommonEncoder(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        // 1. 写入魔数 (4 bytes) - 用于校验
        out.writeInt(MAGIC_NUMBER);

        // 2. 写入包类型 (4 bytes) - 标记是请求还是响应
        if (msg instanceof RpcRequest) {
            out.writeInt(0); // 0 表示请求
        } else {
            out.writeInt(1); // 1 表示响应
        }

        // 3. 写入序列化器编号 (4 bytes) - 比如 Kryo 是 1
        out.writeInt(serializer.getCode());

        // 4. 【核心】序列化对象
        byte[] bytes = serializer.serialize(msg);

        // 5. 写入数据长度 (4 bytes) - 解决粘包的关键！
        out.writeInt(bytes.length);

        // 6. 写入真正的数据
        out.writeBytes(bytes);
    }
}