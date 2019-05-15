package com.wy.rpc.serve.handler;

import com.wy.rpc.common.request.RpcRequest;
import com.wy.rpc.common.response.RpcResponse;
import com.wy.rpc.serve.facade.impl.HiServiceImpl;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @Author: 0x4096.peng@gmail.com
 * @Project: Wy-Rpc
 * @DateTime: 2019/5/11 22:35
 * @Description: RPC 处理器
 */
public class RpcHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcHandler.class);

    private final Map<String, Object> handlerMap;

    public RpcHandler(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, RpcRequest request) {
        RpcResponse response = new RpcResponse();
        response.setRequestId(request.getRequestId());
        try {
            Object result = handle(request);
            response.setResult(result);
        } catch (Throwable t) {
            LOGGER.error("Rpc 处理异常", t);
            response.setError(t);
        }
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private Object handle(RpcRequest request) {
        String className = request.getClassName();
        Object serviceBean = handlerMap.get(className);

//        LOGGER.error(serviceBean.getClass().getD);

        Class serviceClass = (Class) serviceBean;

        String methodName = request.getMethodName();

        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();


        HiServiceImpl hiService = serviceClass;
        return hiService.hi(null);
        /* JDK 代理实现 */
//        Method method = null;
//        try {
//            method = serviceClass.getMethod(methodName, parameterTypes);
//            method.setAccessible(true);
//            return method.invoke(serviceBean, parameters);
//        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
//            LOGGER.error("Rpc 处理异常", e);
//        }
//        return null;

//        FastClass serviceFastClass = FastClass.create(serviceClass);
//        FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
//        try {
//            return serviceFastMethod.invoke(serviceBean, parameters);
//        } catch (InvocationTargetException e) {
//            LOGGER.error("Rpc 处理异常", e);
//        }
//        return null;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error("server caught exception", cause);
        ctx.close();
    }
}
