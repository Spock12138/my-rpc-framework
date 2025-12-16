package com.github.spock12138.rpc.core.serializer;

public interface Serializer {

    /**
     * 序列化：把 Java 对象转成字节数组
     */
    byte[] serialize(Object obj);

    /**
     * 反序列化：把字节数组转回 Java 对象
     * @param bytes 字节数组
     * @param clazz 目标对象的类类型
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);

    /**
     * 获取序列化器的编号 (后续协议中会用到，比如 1 代表 JSON, 2 代表 Kryo)
     */
    int getCode();
}