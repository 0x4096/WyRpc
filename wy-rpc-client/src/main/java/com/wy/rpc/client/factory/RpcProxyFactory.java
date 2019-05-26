package com.wy.rpc.client.factory;


import java.lang.reflect.Proxy;

/**
 * @Author: 0x4096.peng@gmail.com
 * @Project: Wy-Rpc
 * @DateTime: 2019/5/12 0:55
 * @Description: 远程服务代理工厂工具类,通过代理工厂获取接口的代理类
 */
public class RpcProxyFactory {


    /**
     * 创建代理对象
     *
     * @param interfaceClass
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static  <T> T create(Class<?> interfaceClass) {
        RpcMethodProxy invocationHandler = new RpcMethodProxy();
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                invocationHandler);
    }


}
