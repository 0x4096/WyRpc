package com.wy.rpc.common.constant;

/**
 * @Author: 0x4096.peng@gmail.com
 * @Project: Wy-Rpc
 * @DateTime: 2019/5/12 10:10
 * @Description: RPC 常量集
 */
public class RpcConstant {

    /**
     * zookeeper 连接 session 超时时间
     */
    public static final int ZK_SESSION_TIMEOUT = 5_000;

    /**
     * zookeeper 注册地址
     */
    public static final String ZK_REGISTRY_PATH = "/wy-rpc/registry";

    /**
     * RPC 服务端 netty 连接地址
     */
    public static final String WY_RPC_SERVER_PATH = "/wy-rpc/server-path";


    /**
     * zookeeper 数据路径
     */
    public static final String ZK_DATA_PATH = ZK_REGISTRY_PATH + "/data";





}
