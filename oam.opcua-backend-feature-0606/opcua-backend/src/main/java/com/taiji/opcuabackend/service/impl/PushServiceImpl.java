package com.taiji.opcuabackend.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.taiji.opcuabackend.config.NettyConfig;
import com.taiji.opcuabackend.constant.OpcUaConstant;
import com.taiji.opcuabackend.service.PushService;
import com.taiji.opcuabackend.util.RedisUtil;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class PushServiceImpl implements PushService {

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public void pushMsgToSuscriptionOne(Set<String> userIds, String key, String value){

        ConcurrentHashMap<String, Channel> userChannelMap = NettyConfig.getUserChannelMap();

        String subscriptionNode = new StringBuffer(OpcUaConstant.SUBSCRIPTION_NODES_REDIS_KEY).append("|").append(key).toString();


        //初始化返回值
        HashMap<String, String> responseMap = new HashMap<>();
        responseMap.put("nodes", redisUtil.get(key));
        responseMap.put("status","0");
        responseMap.put("msg","success");

        for (String userId: userIds) {

            //如果websocket通道还存在则推送信息
            Channel channel = userChannelMap.get(userId);
            if(channel != null){
                channel.writeAndFlush(new TextWebSocketFrame(new JSONObject(responseMap).toString()));
            }
        }
    }

    @Override
    public void pushMsgToSuscriptionUsers(String msg) {
        NettyConfig.getChannelGroup().writeAndFlush(new TextWebSocketFrame(msg));
    }
}