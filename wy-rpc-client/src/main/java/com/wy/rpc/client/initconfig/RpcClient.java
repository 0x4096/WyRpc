package com.wy.rpc.client.initconfig;

import com.wy.rpc.client.discovery.RpcServiceDiscovery;
import com.wy.rpc.client.factory.RemoteServiceProxyFactory;
import com.wy.rpc.client.handler.RpcProxy;
import com.wy.rpc.client.util.ManualRegistBeanUtil;
import com.wy.rpc.common.annotation.RpcReference;
import com.wy.rpc.common.annotation.RpcService;
import com.wy.rpc.common.code.decode.RpcDecoder;
import com.wy.rpc.common.code.encode.RpcEncoder;
import com.wy.rpc.common.request.RpcRequest;
import com.wy.rpc.common.response.RpcResponse;
import com.wy.rpc.common.util.AnnotationUtil;
import com.wy.rpc.common.util.ClassUtil;
import com.wy.rpc.facade.HiService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: 0x4096.peng@gmail.com
 * @Project: Wy-Rpc
 * @DateTime: 2019/5/11 22:58
 * @Description:
 */
@Configuration
public class RpcClient extends InstantiationAwareBeanPostProcessorAdapter implements ApplicationContextAware, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcClient.class);

    /**
     * 主机 host
     */
    private String host;

    /**
     * 端口
     */
    private int port;


    private Map<String, Object> handlerMap = new HashMap<>();


    private RpcResponse response;

    private final Object obj = new Object();

    private RpcServiceDiscovery rpcServiceDiscovery;

//    @Override
//    public void channelRead0(ChannelHandlerContext ctx, RpcResponse response) {
//        this.response = response;
//
//        synchronized (obj) {
//            // 收到响应，唤醒线程
//            obj.notifyAll();
//        }
//    }



//    public RpcResponse send(RpcRequest request) throws Exception {
//        EventLoopGroup group = new NioEventLoopGroup();
//        try {
//            Bootstrap bootstrap = new Bootstrap();
//            bootstrap.group(group).channel(NioSocketChannel.class)
//                    .handler(new ChannelInitializer<SocketChannel>() {
//                        @Override
//                        public void initChannel(SocketChannel channel) throws Exception {
//                            channel.pipeline()
//                                    /* 将 RPC 请求进行编码（为了发送请求） */
//                                    .addLast(new RpcEncoder(RpcRequest.class))
//                                    /* 将 RPC 响应进行解码（为了处理响应） */
//                                    .addLast(new RpcDecoder(RpcResponse.class))
//                                    /* 使用 RpcClient 发送 RPC 请求 */
//                                    .addLast(RpcClient.this);
//                        }
//                    })
//                    .option(ChannelOption.SO_KEEPALIVE, true);
//
//            ChannelFuture future = bootstrap.connect(host, port).sync();
//            future.channel().writeAndFlush(request).sync();
//
//            synchronized (obj) {
//                /* 未收到响应，使线程等待 */
//                obj.wait();
//            }
//
//            if (response != null) {
//                future.channel().closeFuture().sync();
//            }
//            return response;
//        } finally {
//            group.shutdownGracefully();
//        }
//    }

//    @Override
//    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
//        LOGGER.error("client caught exception", cause);
//        ctx.close();
//    }



    @Override
    public void afterPropertiesSet() throws Exception {
        /* 服务注册发现 */
        rpcServiceDiscovery = new RpcServiceDiscovery();
        rpcServiceDiscovery.discover("127.0.0.1:2181");
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

//      createProxy(applicationContext);

    }

    @Override
    public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {


        ReflectionUtils.doWithFields(bean.getClass(), new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                if (field.isAnnotationPresent(RpcReference.class)) {
                    Class referenceField = field.getType();

                    Object proxyObject = RemoteServiceProxyFactory.create(referenceField);

                    // set bean
                    field.setAccessible(true);
                    field.set(bean, proxyObject);
                    LOGGER.error(referenceField.getName());
                    rpcServiceDiscovery.watchNode(referenceField.getName());
                }
            }
        });

        return super.postProcessAfterInstantiation(bean, beanName);
    }


    /**
     * 创建动态代理
     *
     * @param applicationContext
     */
//    private void createProxy(ApplicationContext applicationContext) throws IllegalAccessException {
//        /* 扫描所有的类, 这里暂时处理只扫描 com.wy.rpc.client 包下的类 */
//        List<Class<?>> classList = ClassUtil.getAllClassByPackageName("com.wy.rpc.client");
//
//        /* 获取所有带有 RpcService 注解的类,并注册为 bean */
//        for(Class clazz : classList){
//            /* 扫描每个类中含有 RpcReference 注解的属性 */
//            List<Field> annotationMethod = ClassUtil.getAnnotationField(clazz, RpcReference.class);
//
//            if(CollectionUtils.isNotEmpty(annotationMethod)){
//                for(Field field : annotationMethod){
//                    /* 创建代理并写入 Spring 容器? */
//                    Class interfaceClazz = field.getType();
//                    /* 动态代理创建对象 */
//                    Object proxyObject = RemoteServiceProxyFactory.create(interfaceClazz);
//                    field.setAccessible(true);
//                    field.set(interfaceClazz, proxyObject);
//
//
//
//                    /* 创建 bean */
//                    ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext)applicationContext;
//
//                    DefaultListableBeanFactory beanFactory =
//                            (DefaultListableBeanFactory) configurableApplicationContext.getBeanFactory();
//                    BeanDefinitionBuilder beanDefinitionBuilder =
//                            BeanDefinitionBuilder.genericBeanDefinition(proxyObject.getClass());
//
//                    beanDefinitionBuilder.addConstructorArgValue(Proxy.getInvocationHandler(proxyObject));
//
//                    AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
//                    beanDefinition.setBeanClass(proxyObject.getClass());
//                    // com.wy.rpc.facade.HiService
//
//                    beanFactory.registerBeanDefinition("hiService", beanDefinitionBuilder.getRawBeanDefinition());
//
//
//                    // ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext)applicationContext;
//                    // ManualRegistBeanUtil.registerBean(configurableApplicationContext, proxyClazss.getName(), proxyObject.getClass());
//
//                }
//
//            }
//
//        }
//    }

}
