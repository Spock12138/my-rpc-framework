package com.github.spock12138.rpc.core.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class KryoSerializer implements Serializer {

    // 使用 ThreadLocal 解决 Kryo 线程不安全的问题
    private static final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        // 支持循环引用（比如 A 引用 B，B 引用 A）
        kryo.setReferences(true);
        // 关闭强制注册（如果不关闭，每一个类都必须先注册才能用，太麻烦）
        kryo.setRegistrationRequired(false);
        return kryo;
    });

    @Override
    public byte[] serialize(Object obj) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             Output output = new Output(byteArrayOutputStream)) {

            Kryo kryo = kryoThreadLocal.get();
            // 将对象写入 output
            kryo.writeObject(output, obj);
            kryoThreadLocal.remove();

            return output.toBytes();
        } catch (Exception e) {
            throw new RuntimeException("序列化失败", e);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             Input input = new Input(byteArrayInputStream)) {

            Kryo kryo = kryoThreadLocal.get();
            // 从 input 读取对象
            Object o = kryo.readObject(input, clazz);
            kryoThreadLocal.remove();

            return clazz.cast(o);
        } catch (Exception e) {
            throw new RuntimeException("反序列化失败", e);
        }
    }

    @Override
    public int getCode() {
        return 1; // 我们暂时规定 1 代表 Kryo
    }
}