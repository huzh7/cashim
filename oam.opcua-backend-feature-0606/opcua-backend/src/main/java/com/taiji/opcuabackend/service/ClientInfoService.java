package com.taiji.opcuabackend.service;

import com.taiji.opcuabackend.entity.AjaxResult;
import com.taiji.opcuabackend.entity.ClientInfo;

import java.util.List;

public interface ClientInfoService {

    /**
     * 查询客户端信息
     * @param clientInfo
     */
    public List<ClientInfo> getClientInfo(ClientInfo clientInfo) throws Exception;

    /**
     * 修改客户端信息
     * @param clientInfo
     */
    public void modifyClientInfoChannelStatus(ClientInfo clientInfo) throws Exception;

    /**
     * 清理redis客户端信息
     *
     */
    public void cleanRedisClientInfoChannel() throws Exception;

    /**
     *
     * @param token
     * @param status
     * @throws Exception
     */
    public void saveClientInfoStatus(String token, int status) throws Exception;

}
