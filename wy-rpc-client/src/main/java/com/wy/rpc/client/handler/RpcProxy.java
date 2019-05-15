package com.wy.rpc.client.handler;

import com.wy.rpc.client.discovery.RpcServiceDiscovery;
import com.wy.rpc.client.initconfig.RpcClient;
import com.wy.rpc.common.request.RpcRequest;
import com.wy.rpc.common.response.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * @Author: 0x4096.peng@gmail.com
 * @Project: Wy-Rpc
 * @DateTime: 2019/5/11 22:54
 * @Description: RPC 代理, 使用 Java 提供的动态代理技术实现 RPC 代理（当然也可以使用 CGLib 来实现）
 */
public class RpcProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcProxy.class);

    @Value("${wy.rpc.registry.address}")
    private String serverAddress;


    private RpcServiceDiscovery rpcServiceDiscovery;

    public RpcProxy(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public RpcProxy(RpcServiceDiscovery rpcServiceDiscovery) {
        this.rpcServiceDiscovery = rpcServiceDiscovery;
    }



//    @SuppressWarnings("unchecked")
//    public <T> T create(Class<?> interfaceClass) {
//        return (T) Proxy.newProxyInstance(
//                interfaceClass.getClassLoader(),
//                new Class<?>[]{interfaceClass},
//                new InvocationHandler() {
//                    @Override
//                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//                        /* 创建并初始化 RPC 请求 */
//                        RpcRequest request = new RpcRequest();
//                        request.setRequestId(UUID.randomUUID().toString());
//                        request.setClassName(method.getDeclaringClass().getName());
//                        request.setMethodName(method.getName());
//                        request.setParameterTypes(method.getParameterTypes());
//                        request.setParameters(args);
//
//                        rpcServiceDiscovery = new RpcServiceDiscovery(serverAddress);
//                        /* 发现服务 */
//                        serverAddress = rpcServiceDiscovery.discover();
//
//                        String[] array = serverAddress.split(":");
//                        String host = array[0];
//                        int port = Integer.parseInt(array[1]);
//
//                        /* 初始化 RPC 客户端 */
//                        RpcClient client = new RpcClient(host, port);
//                        /*  通过 RPC 客户端发送 RPC 请求并获取 RPC 响应 */
//                        RpcResponse response = null;
//                        try {
//                            response = client.send(request);
//                        } catch (Exception e) {
//                            LOGGER.error("RpcProxy 异常", e);
//                        }
//
//                        if ( ! response.isSuccess()) {
//                            throw response.getError();
//                        } else {
//                            return response.getResult();
//                        }
//                    }
//                }
//        );
//    }

}
