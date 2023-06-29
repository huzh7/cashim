package com.taiji.opcuabackend.service.impl;

import com.taiji.opcuabackend.config.NettyConfig;
import com.taiji.opcuabackend.constant.OpcUaConstant;
import com.taiji.opcuabackend.entity.AjaxResult;
import com.taiji.opcuabackend.entity.ClientInfo;
import com.taiji.opcuabackend.mapper.ClientInfoMapper;
import com.taiji.opcuabackend.service.ClientInfoService;
import com.taiji.opcuabackend.service.RegitserService;
import com.taiji.opcuabackend.util.RedisUtil;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ClientInfoServiceImpl implements ClientInfoService {

    @Autowired
    private ClientInfoMapper clientInfoMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public List<ClientInfo> getClientInfo(ClientInfo clientInfo) throws Exception {

        if(clientInfo == null){
            throw new Exception("clientInfo参数为空！");
        }
        HashMap<String, Object> param = new HashMap<>();

        if(clientInfo.getClient() != null){
            param.put("client", clientInfo.getClient());
        }
        if(clientInfo.getIpAddr()!= null){
            param.put("ip_addr", clientInfo.getIpAddr());
        }
        if(clientInfo.getToken() != null){
            param.put("token", clientInfo.getToken());
        }

        List<ClientInfo> ciList = clientInfoMapper.selectByMap(param);
        return ciList;

    }

    @Override
    public void modifyClientInfoChannelStatus(ClientInfo clientInfo) throws Exception {
        if(clientInfo == null){
            throw new Exception("clientInfo参数为空！");
        }
        HashMap<String, Object> param = new HashMap<>();

        if(clientInfo.getClient() != null){
            param.put("client", clientInfo.getClient());
        }
        if(clientInfo.getToken() != null){
            param.put("token", clientInfo.getToken());
        }
        List<ClientInfo> clientInfoList= clientInfoMapper.selectByMap(param);
        if (clientInfoList.size() > 1){
            throw new Exception("client数据库数据为多条，请检查数据");
        }
        ClientInfo ci = clientInfoList.get(0);
        ci.setIsChannelOn(clientInfo.getIsChannelOn());
        clientInfoMapper.updateById(ci);
    }

    @Override
    public void cleanRedisClientInfoChannel() throws Exception {

        log.info("-------------------------------开始清理redis中的客户端信息start-------------------------------");

        ConcurrentHashMap<String, Channel> userChannelMap = NettyConfig.getUserChannelMap();

        Set<String> resultKeySet = redisUtil.keys(OpcUaConstant.SUBSCRIPTION_NODES_REDIS_KEY +"*");

        for (String resultKey: resultKeySet) {

            Set<String> resultValueSet = redisUtil.getSet(resultKey);
            List<String> delValues = new ArrayList<>();
            for (String resultValue: resultValueSet) {
                Channel channel = userChannelMap.get(resultValue);
                if(channel == null){//客户端订阅断开超过5分钟，则清除节点订阅信息
                    delValues.add(resultValue);
                }
            }
            if(delValues != null && delValues.size() != 0){
                redisUtil.delSet(resultKey, delValues.toArray(new String[0]));
            }
        }
    }

    @Override
    public void saveClientInfoStatus(String token, int status) throws Exception {
        if(token == null){
            throw new Exception("token参数为空！");
        }
        HashMap<String, Object> param = new HashMap<>();
        param.put("token", token);
        List<ClientInfo> clientInfoList= clientInfoMapper.selectByMap(param);
        if (clientInfoList.size() == 0){
            throw new Exception("token对应client数据库数据不存在，请检查数据，token： " + token);
        }
        if (clientInfoList.size() > 1){
            throw new Exception("client数据库数据为多条，请检查数据，token： " + token);
        }
        ClientInfo ci = clientInfoList.get(0);
        ci.setStatus(status);
        clientInfoMapper.updateById(ci);
    }
}