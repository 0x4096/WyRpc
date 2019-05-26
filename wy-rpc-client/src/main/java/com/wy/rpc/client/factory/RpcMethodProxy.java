package com.wy.rpc.client.factory;

import com.wy.rpc.client.netty.NettyClient;
import com.wy.rpc.client.netty.RpcClient;
import com.wy.rpc.common.request.RpcRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * @Author: peng.zhup
 * @Project: wy-rpc
 * @DateTime: 2019/5/19 22:01
 * @Description:
 */
@Component
public class RpcMethodProxy implements InvocationHandler {

    @Autowired
    private NettyClient nettyClient;

    /**
     * 通过代理对象发送请求
     *
     * @param proxy
     * @param method
     * @param args
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class.equals(method.getDeclaringClass())) {
            try {
                return method.invoke(this, args);
            } catch (Throwable t) {
                t.printStackTrace();
            }
            //如果传进来的是一个接口（核心)
        } else {
            return run(proxy, method, args);
        }
        return null;
    }



    public Object run(Object object, Method method, Object[] args) throws InterruptedException {
        //TODO
        RpcRequest rpcRequest = new RpcRequest();

        rpcRequest.setMethodName(method.getName());
        rpcRequest.setRequestId(UUID.randomUUID().toString());
        rpcRequest.setClassName(method.getDeclaringClass().getName());
        rpcRequest.setParameterTypes(method.getParameterTypes());
        rpcRequest.setParameters(args);

        Object res = nettyClient.send(rpcRequest);

        return res;
    }



}
