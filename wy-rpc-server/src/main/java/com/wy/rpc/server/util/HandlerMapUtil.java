package com.wy.rpc.server.util;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: peng.zhup
 * @Project: wy-rpc
 * @DateTime: 2019/5/16 22:39
 * @Description:
 */
public class HandlerMapUtil {


    private static ConcurrentHashMap<String, Object> handlerMap = new ConcurrentHashMap<>();


    public static void put(String key, Object clazz){
        handlerMap.put(key, clazz);
    }

    public static Object get(String key){
        return handlerMap.get(key);
    }

}
