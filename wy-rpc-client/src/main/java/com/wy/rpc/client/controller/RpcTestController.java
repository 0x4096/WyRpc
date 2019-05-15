package com.wy.rpc.client.controller;

import com.wy.rpc.common.annotation.RpcReference;
import com.wy.rpc.facade.service.HiService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * @Author: 0x4096.peng@gmail.com
 * @Project: Wy-Rpc
 * @DateTime: 2019/5/11 23:09
 * @Description:
 */
@RestController
public class RpcTestController {

    @RpcReference
    private HiService hiService;


    /**
     * 测试
     *
     * @param message
     * @return
     */
    @RequestMapping(value = "/test")
    public String test(@RequestParam(value = "message") String message){
        return hiService.hi(message);
    }


}
