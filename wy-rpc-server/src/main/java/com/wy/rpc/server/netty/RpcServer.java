package com.wy.rpc.server.netty;

import com.wy.rpc.common.annotation.RpcService;
import com.wy.rpc.common.code.decode.RpcDecoder;
import com.wy.rpc.common.code.encode.RpcEncoder;
import com.wy.rpc.common.request.RpcRequest;
import com.wy.rpc.common.response.RpcResponse;
import com.wy.rpc.common.util.AnnotationUtil;
import com.wy.rpc.common.util.ClassUtil;
import com.wy.rpc.server.handler.RpcServerHandler;
import com.wy.rpc.server.registry.RpcServiceRegistry;
import com.wy.rpc.server.util.HandlerMapUtil;
import com.wy.rpc.server.util.ManualRegistBeanUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author: 0x4096.peng@gmail.com
 * @Project: Wy-Rpc
 * @DateTime: 2019/5/11 21:25
 * @Description: RPC 服务初始化
 */
@Configuration
public class RpcServer implements ApplicationContextAware, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);

    @Value("${wy.rpc.server.address}")
    private String serverAddress;


    @Autowired
    private RpcServiceRegistry rpcServiceRegistry;


    private static final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private static final EventLoopGroup workerGroup = new NioEventLoopGroup(4);


    /**
     *
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }



    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        createBean(applicationContext);
    }


    public void start(){
        final RpcServerHandler handler = new RpcServerHandler();
        new Thread(() -> {
            try {
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workerGroup).
                        channel(NioServerSocketChannel.class).
                        option(ChannelOption.SO_BACKLOG,1024).
                        childOption(ChannelOption.SO_KEEPALIVE,true).
                        childOption(ChannelOption.TCP_NODELAY,true).
                        childHandler(new ChannelInitializer<SocketChannel>() {
                            //创建NIOSocketChannel成功后，在进行初始化时，将它的ChannelHandler设置到ChannelPipeline中，用于处理网络IO事件
                            protected void initChannel(SocketChannel channel) throws Exception {
                                ChannelPipeline pipeline = channel.pipeline();
                                pipeline.addLast(new IdleStateHandler(0, 4, 0, TimeUnit.SECONDS));
                                pipeline.addLast(new RpcDecoder(RpcRequest.class));
                                pipeline.addLast(new RpcEncoder(RpcResponse.class));
                                pipeline.addLast(handler);
                            }
                        });
                String[] array = serverAddress.split(":");
                String host = array[0];
                int port = Integer.parseInt(array[1]);
                ChannelFuture cf = bootstrap.bind(host, port).sync();
                LOGGER.info("RPC 服务器启动,监听端口: "+port);

                // rpcServiceRegistry.register(serverAddress);

                //等待服务端监听端口关闭
                cf.channel().closeFuture().sync();
            } catch (Exception e) {
                e.printStackTrace();
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        }).start();
    }


    /**
     * 手动创建带有 RpcService 注解的 bean
     *
     * @param applicationContext
     */
    private void createBean(ApplicationContext applicationContext) {
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
                HandlerMapUtil.put(classes[0].getName(), clazz);
            }
        }
    }
}
