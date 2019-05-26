package com.wy.rpc.client.netty;

import com.wy.rpc.client.discovery.RpcServiceDiscovery;
import com.wy.rpc.client.factory.RpcProxyFactory;
import com.wy.rpc.client.handler.RpcClientHandler;
import com.wy.rpc.common.annotation.RpcReference;
import com.wy.rpc.common.code.decode.RpcDecoder;
import com.wy.rpc.common.code.encode.RpcEncoder;
import com.wy.rpc.common.request.RpcRequest;
import com.wy.rpc.common.response.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author: peng.zhup
 * @Project: wy-rpc
 * @DateTime: 2019/5/19 22:14
 * @Description:
 */
@Configuration
public class RpcClient extends InstantiationAwareBeanPostProcessorAdapter implements ApplicationContextAware, InitializingBean {


    private static final Logger LOGGER = LoggerFactory.getLogger(RpcClient.class);

    private EventLoopGroup group = new NioEventLoopGroup(1);

    private Bootstrap bootstrap = new Bootstrap();

    private Channel channel;


    @Autowired
    private RpcServiceDiscovery rpcServiceDiscovery;


    @Override
    public void afterPropertiesSet() {
        /* 服务注册发现 */
        rpcServiceDiscovery.init();
        // start();
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

//      createProxy(applicationContext);

    }


    /**
     * 使用动态代理设置bean
     *
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
        ReflectionUtils.doWithFields(bean.getClass(), new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                if (field.isAnnotationPresent(RpcReference.class)) {
                    Class referenceField = field.getType();
                    Object proxyObject = RpcProxyFactory.create(referenceField);
                    // set bean
                    field.setAccessible(true);
                    field.set(bean, proxyObject);
                    /* zookeeper 监听节点 */
                    rpcServiceDiscovery.watchNode(referenceField.getName());
                }
            }
        });

        return super.postProcessAfterInstantiation(bean, beanName);
    }


    public void start(){
        String[] serverAddrList = rpcServiceDiscovery.serverAddr.split(":");
        String host = serverAddrList[0];
        int port = Integer.valueOf(serverAddrList[1]);

        RpcClientHandler rpcClientHandler = new RpcClientHandler();
        bootstrap.group(group).
                channel(NioSocketChannel.class).
                option(ChannelOption.TCP_NODELAY, true).
                option(ChannelOption.SO_KEEPALIVE,true).
                remoteAddress(host, port).
                handler(new ChannelInitializer<SocketChannel>() {
                    //创建NIOSocketChannel成功后，在进行初始化时，将它的ChannelHandler设置到ChannelPipeline中，用于处理网络IO事件
                    protected void initChannel(SocketChannel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast(new IdleStateHandler(0, 0, 30, TimeUnit.SECONDS));
                        pipeline.addLast(new RpcDecoder(RpcRequest.class));
                        pipeline.addLast(new RpcEncoder(RpcResponse.class));
                        pipeline.addLast("handler", rpcClientHandler);
                    }
                });
        try {
            channel = bootstrap.connect().sync().channel();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOGGER.info("RPC 客户端启动成功");
    }

    public Object send(RpcRequest request) {

        try {
            channel.writeAndFlush(request).sync();
        } catch (InterruptedException e) {
            LOGGER.error("", e);
        }

        return "";
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
