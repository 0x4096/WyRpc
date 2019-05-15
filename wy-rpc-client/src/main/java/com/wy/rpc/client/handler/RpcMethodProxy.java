package com.wy.rpc.client.handler;

import com.wy.rpc.client.initconfig.RpcClient;
import com.wy.rpc.common.code.decode.RpcDecoder;
import com.wy.rpc.common.code.encode.RpcEncoder;
import com.wy.rpc.common.request.RpcRequest;
import com.wy.rpc.common.response.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopProxy;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * @Author: 0x4096.peng@gmail.com
 * @Project: Wy-Rpc
 * @DateTime: 2019/5/12 0:45
 * @Description:
 */
public class RpcMethodProxy extends SimpleChannelInboundHandler<RpcResponse> implements InvocationHandler {

    // TODO
    /**
     * 主机 host
     */
    private String host = "127.0.0.1";

    /**
     * 端口
     */
    private int port = 2182;

    private RpcResponse response;

    private final Object obj = new Object();

    @Override
    public Object invoke(Object proxy, Method method, Object[] args)  throws Throwable {
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

    /**
     * 实现接口的核心方法
     *
     * @param method
     * @param args
     * @return
     */
    public Object run(Object object, Method method, Object[] args) throws Exception {
        //TODO

        RpcRequest rpcRequest = new RpcRequest();

        rpcRequest.setMethodName(method.getName());
        rpcRequest.setRequestId(UUID.randomUUID().toString());
        rpcRequest.setClassName(method.getDeclaringClass().getName());
        rpcRequest.setParameterTypes(method.getParameterTypes());
        rpcRequest.setParameters(args);

        return send(rpcRequest);
        //如远程http调用
        //如远程方法调用（rmi)
    }

    private RpcResponse send(RpcRequest request) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline()
                                    /* 将 RPC 请求进行编码（为了发送请求） */
                                    .addLast(new RpcEncoder(request.getClass()))
                                    /* 将 RPC 响应进行解码（为了处理响应） */
                                    .addLast(new RpcDecoder(request.getClass()))
                                    /* 使用 RpcClient 发送 RPC 请求 */
                                    .addLast(RpcMethodProxy.this);
                        }
                    })
                    .option(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture future = bootstrap.connect(host, port).sync();
            future.channel().writeAndFlush(request).sync();

            synchronized (obj) {
                /* 未收到响应，使线程等待 */
                obj.wait();
            }

            if (response != null) {
                future.channel().closeFuture().sync();
            }
            return response;
        } finally {
            group.shutdownGracefully();
        }
    }


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
        this.response = rpcResponse;
        synchronized (obj) {
            // 收到响应，唤醒线程
            obj.notifyAll();
        }
    }


    /**
     * 获取代理对象的原始对象
     *
     * @param proxy
     * @return
     * @throws Exception
     */
    private static Object getJdkDynamicProxyTargetObject(Object proxy) throws Exception {
        Field field = proxy.getClass().getSuperclass().getDeclaredField("h");
        field.setAccessible(true);
        // 获取指定对象中此字段的值


        return field.getType();
    }

}
