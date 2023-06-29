package com.taiji.opcuabackend.lisenter;

import com.alibaba.fastjson.JSONObject;
import com.taiji.opcuabackend.constant.OpcUaConstant;
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

import java.util.Set;

/**
 * @author sunzb
 */
@Component
@Data
@Slf4j
public class RedisUpdateAndAddListener implements MessageListener {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private PushService pushService;

    @Autowired
    private DailyValueService dailyValueService;


    @Autowired
    private ErrorNodeInfoService errorNodeInfoService;

    @Override
    public void onMessage(Message message, byte[] pattern) {

        String key = new String(message.getBody());     //操作
        String value = redisUtil.get(key);

        String subscriptionNode = new StringBuffer(OpcUaConstant.SUBSCRIPTION_NODES_REDIS_KEY).append("|").append(key).toString();

        //判断redis中是否已经存在订阅节点，如果没有则新建，如果有则取出进行添加操作
        Set<String> resultSet = redisUtil.getSet(subscriptionNode);
        if(resultSet != null && resultSet.size() != 0){
            pushService.pushMsgToSuscriptionOne(resultSet, key, value);
        }

        //判断更新节点是否在错误节点列表中
        Set<String> errorNodeSet = redisUtil.getSet(OpcUaConstant.ERROR_NODES);
        if(errorNodeSet != null && errorNodeSet.size() != 0){
            String lastNodeName = new String();
            if(key.contains(".")){
                String[] errorNodeArray = key.split("\\.");
                lastNodeName = errorNodeArray[errorNodeArray.length-1];
            }else {
                lastNodeName = key;
            }

            if(errorNodeSet.contains(lastNodeName)){

                JSONObject valueJSONObject = JSONObject.parseObject(value);
                try {
                    dailyValueService.updateDailyValue(key, (String)valueJSONObject.get("nodeValue"));
                    // TODO merge
                    errorNodeInfoService.saveErrorNodeInfo(key, (String)valueJSONObject.get("nodeValue"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

