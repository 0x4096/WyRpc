package com.wy.rpc.server.registry;

import com.wy.rpc.common.constant.RpcConstant;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @Author: 0x4096.peng@gmail.com
 * @Project: Wy-Rpc
 * @DateTime: 2019/5/11 21:26
 * @Description: RPC 服务注册
 */
@Component
public class RpcServiceRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServiceRegistry.class);

    private CuratorFramework client;

    /**
     * zookeeper 注册地址
     */
    @Value("${wy.rpc.registry.address}")
    private String registerAddress;


    /**
     * 服务地址
     *
     * @param serverAddress
     */
    public void register(String serverAddress) {
        if(StringUtils.isBlank(serverAddress)){
            throw new NullPointerException("注册地址不能为null");
        }
        /* 建立连接 */
        connectServer();
        if(client != null){
            /* 创建节点 */
            createServerNode(serverAddress);
        }
    }

    /**
     * 连接 zookeeper
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


    /**
     * 创建 netty 连接地址
     *
     * @param serverAddress
     */
    private void createServerNode(String serverAddress){
        if(isExistNode(RpcConstant.WY_RPC_SERVER_PATH)){
            setData(RpcConstant.WY_RPC_SERVER_PATH, serverAddress);
            return;
        }
        try {
            //使用creatingParentContainersIfNeeded()之后Curator能够自动递归创建所有所需的父节点
            client.create().creatingParentsIfNeeded().forPath(RpcConstant.WY_RPC_SERVER_PATH, serverAddress.getBytes("UTF-8"));
        } catch (Exception e) {
            LOGGER.error("创建 zookeeper 节点错误", e);
        }
    }


    /**
     * 检查地址是否存在
     *
     * @param path
     * @return
     */
    public boolean isExistNode(final String path) {
        client.sync();
        try {
            return null != client.checkExists().forPath(path);
        } catch (Exception ex) {
            LOGGER.error("zookeeper 检查地址是否存在异常", ex);
            return false;
        }
    }


    /**
     * 设置节点数据
     *
     * @param path
     * @param data
     */
    public void setData(String path, String data) {
        try {
            client.setData().forPath(path, data.getBytes("UTF-8"));
        } catch (Exception e) {
            LOGGER.error("zookeeper 设置节点异常", e);
        }
    }

}
