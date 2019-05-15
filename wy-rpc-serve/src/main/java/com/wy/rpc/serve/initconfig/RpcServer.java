package com.wy.rpc.serve.initconfig;

import com.wy.rpc.common.annotation.RpcService;
import com.wy.rpc.common.code.decode.RpcDecoder;
import com.wy.rpc.common.code.encode.RpcEncoder;
import com.wy.rpc.common.request.RpcRequest;
import com.wy.rpc.common.response.RpcResponse;
import com.wy.rpc.common.util.AnnotationUtil;
import com.wy.rpc.common.util.ClassUtil;
import com.wy.rpc.serve.handler.RpcHandler;
import com.wy.rpc.serve.registry.RpcServiceRegistry;
import com.wy.rpc.serve.util.ManualRegistBeanUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: 0x4096.peng@gmail.com
 * @Project: Wy-Rpc
 * @DateTime: 2019/5/11 21:25
 * @Description: RPC 服务初始化
 */
@Configuration
public class RpcServer implements ApplicationContextAware, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);

    @Value("${wy.rpc.registry.address}")
    private String serverAddress;


    private RpcServiceRegistry rpcServiceRegistry;

    /**
     * 存放接口名与服务对象之间的映射关系
     */
    private Map<String, Object> handlerMap = new HashMap<>();


    /**
     *
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline()
                                    /* 将 RPC 请求进行解码（为了处理请求） */
                                    .addLast(new RpcDecoder(RpcRequest.class))
                                    /* 将 RPC 响应进行编码（为了返回响应） */
                                    .addLast(new RpcEncoder(RpcResponse.class))
                                    /* 处理 RPC 请求 */
                                    .addLast(new RpcHandler(handlerMap));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            String[] array = serverAddress.split(":");
            String host = array[0];
            int port = Integer.parseInt(array[1]);
            // TODO
            ChannelFuture future = bootstrap.bind(host, 2182).sync();
            LOGGER.info("netty server started on port " + port);

            /* 监听服务器关闭监听 */
            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }



    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        createBean(applicationContext);
    }


    /**
     * 手动创建带有 RpcService 注解的 bean
     *
     * @param applicationContext
     */
    private void createBean(ApplicationContext applicationContext) {
        rpcServiceRegistry = new RpcServiceRegistry(serverAddress);
        /* 注册服务地址 */
        rpcServiceRegistry.register(serverAddress);

        /* 扫描所有的类, 这里暂时处理只扫描 com.wy.rpc.serve.facade.impl 包下的类 */
        List<Class<?>> classList = ClassUtil.getAllClassByPackageName("com.wy.rpc.serve.facade.impl");

        /* 获取所有带有 RpcService 注解的类,并注册为 bean */
        for(Class clazz : classList){
            Class<?>[] classes = clazz.getInterfaces();

            boolean is = AnnotationUtil.isAnnotation(clazz, RpcService.class);
            if(is){
                /* 创建 bean */
                ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext)applicationContext;
                ManualRegistBeanUtil.registerBean(configurableApplicationContext, clazz.getName(), clazz);
                /* 创建 zk 节点 */
                rpcServiceRegistry.createNode(clazz.getName());
                handlerMap.put(classes[0].getName(), clazz);
            }
        }
    }
}
