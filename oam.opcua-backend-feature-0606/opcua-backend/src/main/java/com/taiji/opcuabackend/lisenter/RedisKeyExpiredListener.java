package com.taiji.opcuabackend.lisenter;

import com.alibaba.fastjson.JSONObject;
import com.taiji.opcuabackend.constant.OpcUaConstant;
import com.taiji.opcuabackend.entity.ClientInfo;
import com.taiji.opcuabackend.service.ClientInfoService;
import com.taiji.opcuabackend.service.DailyValueService;
import com.taiji.opcuabackend.service.ErrorNodeInfoService;
import com.taiji.opcuabackend.service.PushService;
import com.taiji.opcuabackend.util.RedisUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * @author sunzb
 */
@Component
@Data
@Slf4j
public class RedisKeyExpiredListener implements MessageListener {

    @Autowired
    private ClientInfoService clientInfoService;

    @Override
    public void onMessage(Message message, byte[] pattern) {

       log.info("RedisKeyExpiredListener: " + message.toString());
        String key = new String(message.getBody());     //操作

        String token = null;
        //分解key，获取节点名字
        if (key.contains("|")) {
            String[] keyArray = key.split("\\|");
            token = keyArray[1];

            //修改客户端状态
            try {
                clientInfoService.saveClientInfoStatus(token, OpcUaConstant.CLIENT_STATUS_EXPIRED);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}

