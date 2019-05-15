package com.wy.rpc.serve.registry;

import com.wy.rpc.common.constant.RpcConstant;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * @Author: 0x4096.peng@gmail.com
 * @Project: Wy-Rpc
 * @DateTime: 2019/5/11 21:26
 * @Description: RPC 服务注册
 */
public class RpcServiceRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServiceRegistry.class);

    private CountDownLatch latch = new CountDownLatch(1);

    private String registryAddress;

    private CuratorFramework client;

    public RpcServiceRegistry(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    /**
     * 注册服务
     *
     * @param registryAddress
     */
    public void register(String registryAddress) {
        if(StringUtils.isBlank(registryAddress)){
            throw new NullPointerException("注册地址不能为null");
        }

        connectServer(registryAddress);
    }

    /**
     * 连接 zookeeper
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
     * 创建 zk 节点
     *
     * @param zk
     * @param data
     */
    public void createNode(ZooKeeper zk, String data) {
        byte[] bytes = data.getBytes();

        try {
            String path = zk.create(RpcConstant.ZK_DATA_PATH, bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            LOGGER.info("create zookeeper node ({} => {})", path, data);
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error("创建 zookeeper 节点异常", e);
        }
    }


    /**
     * 创建 zookeeper 节点
     *
     * @param nodeData
     */
    public void createNode(String nodeData) {
        if(StringUtils.isBlank(nodeData)){
            throw new NullPointerException("zookeeper 节点不能为空");
        }

        if(isExistNode(RpcConstant.ZK_REGISTRY_PATH)){
            return;
        }

        try {
            //使用creatingParentContainersIfNeeded()之后Curator能够自动递归创建所有所需的父节点
            client.create().creatingParentsIfNeeded().forPath(RpcConstant.ZK_REGISTRY_PATH, nodeData.getBytes("UTF-8"));
        } catch (Exception e) {
            LOGGER.error("创建 zookeeper 节点错误", e);
        }
    }

    public boolean isExistNode(final String path) {
        client.sync();
        try {
            return null != client.checkExists().forPath(path);
        } catch (Exception ex) {
            return false;
        }
    }

}
