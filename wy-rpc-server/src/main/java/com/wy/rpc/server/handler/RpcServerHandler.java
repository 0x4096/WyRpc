package com.wy.rpc.server.handler;

import com.wy.rpc.common.request.RpcRequest;
import com.wy.rpc.common.response.RpcResponse;
import com.wy.rpc.server.util.HandlerMapUtil;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @Author: 0x4096.peng@gmail.com
 * @Project: Wy-Rpc
 * @DateTime: 2019/5/11 22:35
 * @Description: RPC 处理器
 */
@ChannelHandler.Sharable
public class RpcServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServerHandler.class);



    @Override
    public void channelActive(ChannelHandlerContext ctx)   {
        LOGGER.info("客户端连接成功!" + ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx)   {
        LOGGER.info("客户端断开连接!{}", ctx.channel().remoteAddress());
        ctx.channel().close();
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)   {
        RpcRequest request = (RpcRequest) msg;

        if ("heartBeat".equals(request.getMethodName())) {
            LOGGER.info("客户端心跳信息..." + ctx.channel().remoteAddress());
        }else{
            LOGGER.info("RPC客户端请求接口:"+request.getClassName()+"   方法名:"+request.getMethodName());
            RpcResponse response = new RpcResponse();
            response.setRequestId(request.getRequestId());
            try {
                Object result = handle(request);
                response.setResult(result);
            } catch (Throwable e) {
                response.setSuccess(false);
                response.setError(e);
                LOGGER.error("RPC Server handle request error", e);
            }
            ctx.writeAndFlush(response);
        }
    }


    private Object handle(RpcRequest request) {
        String className = request.getClassName();
        Object serviceBean = HandlerMapUtil.get(className);

        Class serviceClass = (Class) serviceBean;

        String methodName = request.getMethodName();

        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();


        /* JDK 代理实现 */
        Method method = null;
        try {
            method = serviceClass.getMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method.invoke(serviceBean, parameters);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            LOGGER.error("Rpc 处理异常", e);
        }
        return null;

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
