package com.wy.rpc.common.util;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @Author: 0x4096.peng@gmail.com
 * @Project: Wy-Rpc
 * @DateTime: 2019/5/11 22:24
 * @Description: 序列号工具,使用 Protostuff 实现序列化
 * 使用 Objenesis 来实例化对象，它是比 Java 反射更加强大
 */
public class SerializationUtil {

    private static final ConcurrentMap<Class<?>, Schema<?>> schemaCache = new ConcurrentHashMap<>();

    private static Map<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<>();

    private static Objenesis objenesis = new ObjenesisStd(true);

    private SerializationUtil() {
    }

    @SuppressWarnings("unchecked")
    private static <T> Schema<T> getSchema(Class<T> clazz) {
//        Schema<T> schema = (Schema<T>) cachedSchema.get(cls);
//        if (schema == null) {
//            schema = RuntimeSchema.createFrom(cls);
//            if (schema != null) {
//                cachedSchema.put(cls, schema);
//            }
//        }

        Schema<T> schema = (Schema<T>) schemaCache.get(clazz);
        if (schema == null) {
            // 把可序列化的字段封装到Schema
            Schema<T> newSchema = RuntimeSchema.createFrom(clazz);
            schema = (Schema<T>) schemaCache.putIfAbsent(clazz, newSchema);
            if (schema == null) {
                schema = newSchema;
            }
        }

        return schema;
    }


    /**
     * 序列化
     *
     * @param obj
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> byte[] serialize(T obj) {
        Class<T> cls = (Class<T>) obj.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            Schema<T> schema = getSchema(cls);
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            buffer.clear();
        }
    }


    /**
     * 反序列化
     *
     * @param data
     * @param cls
     * @param <T>
     * @return
     */
    public static <T> T deserialize(byte[] data, Class<T> cls) {
        try {
            T message = objenesis.newInstance(cls);
            Schema<T> schema = getSchema(cls);
            ProtostuffIOUtil.mergeFrom(data, message, schema);
            return message;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }


}
