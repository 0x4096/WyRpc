package com.wy.rpc.client.discovery;

import com.wy.rpc.common.constant.RpcConstant;
import io.netty.util.internal.ThreadLocalRandom;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @Author: 0x4096.peng@gmail.com
 * @Project: Wy-Rpc
 * @DateTime: 2019/5/11 22:45
 * @Description: RPC 服务发现
 */
public class RpcServiceDiscovery {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServiceDiscovery.class);

    private CountDownLatch latch = new CountDownLatch(1);

    private volatile List<String> dataList = new ArrayList<>();

    private CuratorFramework client;




    public String discover(String registryAddress) {
        String data = null;
        int size = dataList.size();
        if (size > 0) {
            if (size == 1) {
                data = dataList.get(0);
                LOGGER.info("using only data: {}", data);
            } else {
                data = dataList.get(ThreadLocalRandom.current().nextInt(size));
                LOGGER.info("using random data: {}", data);
            }
        }
        connectServer(registryAddress);

//        watchNode(zk);

        return data;
    }


    /**
     * 连接 zookeeper 服务
     *
     * @return
     */
    private void connectServer(String registryAddress) {
        /* 多个地址逗号隔开 */
        client = CuratorFrameworkFactory.builder().connectString(registryAddress)
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




}
