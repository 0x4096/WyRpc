package com.wy.rpc.server.facade.impl;

import com.wy.rpc.common.annotation.RpcService;
import com.wy.rpc.facade.service.HiService;

/**
 * @Author: 0x4096.peng@gmail.com
 * @Project: Wy-Rpc
 * @DateTime: 2019/5/11 23:14
 * @Description: RPC 服务接口实现
 */
@RpcService
public class HiServiceImpl implements HiService {


    @Override
    public String hi(String message) {
        return message == null ? "老弟输入点信息试试?" : "嘿,已经收到你的信息: " + message;
    }
}
