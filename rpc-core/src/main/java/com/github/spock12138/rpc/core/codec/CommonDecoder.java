package com.github.spock12138.rpc.core.codec;

import com.github.spock12138.rpc.core.serializer.Serializer;
import com.github.spock12138.rpc.common.entity.RpcRequest;
import com.github.spock12138.rpc.common.entity.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class CommonDecoder extends ByteToMessageDecoder {

    private static final int MAGIC_NUMBER = 0xCAFEBABE;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 1. 校验魔数
        // 协议头总共 16 字节 (4魔数 + 4类型 + 4序列化器 + 4长度)
        // 如果可读字节数少于 16，说明头都没发完，等待下一次传输
        if (in.readableBytes() < 16) {
            return;
        }

        // 标记当前读取位置，如果后面发现数据不够，需要重置读索引回退
        in.markReaderIndex();

        int magic = in.readInt();
        if (magic != MAGIC_NUMBER) {
            ctx.close();
            throw new IllegalArgumentException("识别不了的包，魔数不对！");
        }

        int packageType = in.readInt();
        int serializerCode = in.readInt();
        int length = in.readInt();

        // 2. 【核心】校验数据包完整性
        // 如果当前剩下的字节数 < 数据体的长度，说明发生了“半包”
        // 我们重置读取位置，什么都不做，等待 Netty 下次拼接好更多数据再来调我们
        if (in.readableBytes() < length) {
            in.resetReaderIndex();
            return;
        }

        // 3. 读取数据体
        byte[] bytes = new byte[length];
        in.readBytes(bytes);

        // 4. 获取序列化器 (这里为了演示简单，我们暂时假设只有 Kryo，实际应该用工厂模式根据 serializerCode 获取)
        // 这里的 serializer 应该和你传入 Encoder 的是同一个类型的实例
        // 暂时简单 new 一个，或者通过构造函数传进来，这里为了演示逻辑先简化处理
        Serializer serializer = new com.github.spock12138.rpc.core.serializer.KryoSerializer();

        // 5. 反序列化
        Object obj = null;
        if (packageType == 0) {
            obj = serializer.deserialize(bytes, RpcRequest.class);
        } else {
            obj = serializer.deserialize(bytes, RpcResponse.class);
        }

        out.add(obj);
    }
}