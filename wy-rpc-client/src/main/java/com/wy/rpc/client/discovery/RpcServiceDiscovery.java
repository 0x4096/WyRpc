package com.wy.rpc.client.discovery;

import com.wy.rpc.client.connent.ConnectManage;
import com.wy.rpc.common.constant.RpcConstant;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.internal.ThreadLocalRandom;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @Author: 0x4096.peng@gmail.com
 * @Project: Wy-Rpc
 * @DateTime: 2019/5/11 22:45
 * @Description: RPC 服务发现
 */
@Component
public class RpcServiceDiscovery {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServiceDiscovery.class);

    /**
     * zookeeper 注册地址
     */
    @Value("${wy.rpc.registry.address}")
    private String registerAddress;

    @Autowired
    private ConnectManage connectManage;


    /* 服务地址列表 */
    public String serverAddr;

    private volatile List<String> addressList = new ArrayList<>();


    private CuratorFramework client;


    public void init(){
        connectServer();
        if(client != null){
            /* 服务端 netty 地址 */
            serverAddr = getNodeData(RpcConstant.WY_RPC_SERVER_PATH);
            updateConnectedServer(serverAddr);

        }
    }


    /**
     * 连接 zookeeper 服务
     *
     * @return
     */
    private void connectServer() {
        /* 多个地址逗号隔开 */
        client = CuratorFrameworkFactory.builder().connectString(registerAddress)
                /* 连接超时时间 */
                .sessionTimeoutMs(RpcConstant.ZK_SESSION_TIMEOUT)
                /* 会话超时时间 */
                .connectionTimeoutMs(RpcConstant.ZK_SESSION_TIMEOUT)
                /* 刚开始重试间隔为1秒，之后重试间隔逐渐增加，最多重试不超过三次 */
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();
        client.start();

    }

    /**
     * 监听节点
     *
     * @param nodePath
     */
    public void watchNode(String nodePath) {

    }



    /**
     * 获取节点数据
     *
     * @param path
     * @return
     */
    public String getNodeData(String path){
        if(StringUtils.isBlank(path)){
            throw new IllegalArgumentException("节点路径不能为空");
        }
        try {
            byte[] bytes = client.getData().forPath(path);
            return new String(bytes);
        } catch (Exception e) {
            LOGGER.error("获取节点数据异常", e);
        }
        return null;
    }


    private void updateConnectedServer(String serverAddr){
        connectManage.updateConnectServer(Arrays.asList(serverAddr));
    }
}
