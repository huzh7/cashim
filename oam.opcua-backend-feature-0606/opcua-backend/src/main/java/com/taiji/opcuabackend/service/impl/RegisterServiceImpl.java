package com.taiji.opcuabackend.service.impl;

import com.taiji.opcuabackend.config.OpcUaConfig;
import com.taiji.opcuabackend.constant.OpcUaConstant;
import com.taiji.opcuabackend.entity.AjaxResult;
import com.taiji.opcuabackend.entity.ClientInfo;
import com.taiji.opcuabackend.mapper.ClientInfoMapper;
import com.taiji.opcuabackend.service.RegitserService;
import com.taiji.opcuabackend.util.JwtUtils;
import com.taiji.opcuabackend.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class RegisterServiceImpl implements RegitserService{

    @Autowired
    private ClientInfoMapper clientInfoMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private OpcUaConfig opcUaConfig;
    @Override
    public AjaxResult saveClientInfo(String client, String ip) {

        HashMap<String, Object> param = new HashMap<>();
        param.put("client", client);
        param.put("ip_addr", ip);

        Map<String,String> payload =  new HashMap<>();
        payload.put("client",client);
        payload.put("ip_addr",ip);

        List<ClientInfo> resultList = clientInfoMapper.selectByMap(param);
        if(resultList == null || resultList.size() == 0){
            //如果不存在，生成token
            ClientInfo ci=  new ClientInfo();
            ci.setClient(client);
            ci.setIpAddr(ip);
            ci.setStatus(OpcUaConstant.CLIENT_STATUS_NOMAL);
            ci.setRegisterDate(new Date());

            String token = JwtUtils.createToken(payload, opcUaConfig.getTokenTimeout());
            ci.setToken(token);
            clientInfoMapper.insert(ci);

            //用"|"将client和token拼接后，存入redis，并设置过期时间为OpuaServer的过期时间
            String tokenKey = client + "|" + token;
            redisUtil.set(tokenKey, token, opcUaConfig.getTokenTimeout());

            return AjaxResult.success("token",token);
        }else {
            //如果已经存在，判断是否过期，如果过期，重新生成token
            ClientInfo ci = resultList.get(0);
            if (ci.getStatus() == OpcUaConstant.CLIENT_STATUS_NOMAL || ci.getStatus() == OpcUaConstant.CLIENT_STATUS_EXPIRED){

                //已过期，重新生成token
                String token = JwtUtils.createToken(payload, opcUaConfig.getTokenTimeout());
                ci.setToken(token);
                ci.setStatus(OpcUaConstant.CLIENT_STATUS_NOMAL);
                clientInfoMapper.updateById(ci);

                //用"|"将client和token拼接后，存入redis，并设置过期时间为OpuaServer的过期时间
                String tokenKey = client + "|" + token;
                redisUtil.set(tokenKey, token, opcUaConfig.getTokenTimeout());
                return new AjaxResult(201,"操作成功",token);
            }else {
                return AjaxResult.error("客户端已被禁用");
            }
        }
    }
}