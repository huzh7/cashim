package com.taiji.opcuabackend.service;

import com.taiji.opcuabackend.entity.AjaxResult;

public interface RegitserService {

    /**
     * 注册、保存客户端信息
     * @param client
     * @param ip
     * @return 生成返回token
     */
    public AjaxResult saveClientInfo(String client, String ip) throws Exception;


}
