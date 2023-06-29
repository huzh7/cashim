package com.taiji.opcuabackend.task;

import com.taiji.opcuabackend.service.ClientInfoService;
import com.taiji.opcuabackend.service.ErrorNodeInfoService;
import com.taiji.opcuabackend.service.OpcUaSubscriptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * OPCUA读取节点
 * 启动服务后，进行
 */
@Component
@Slf4j
public class OpcUaReadNodeTask {

    @Autowired
    private OpcUaSubscriptionService opcUaSubscriptionService;

    @Autowired
    private ClientInfoService clientInfoService;

    @Autowired
    private ErrorNodeInfoService errorNodeInfoService;

    /**
     * 订阅OpcUa服务端所有节点
     */
    public void subScriptionAllNode() {
       try {
           opcUaSubscriptionService.opcServerItemSubscription();
        }catch (Exception e){
           e.printStackTrace();
       }
    }

    /**
     * 清理Redis中的客户端信息
     */
    public void cleanRedisClientInfoChannel() {
        try {
            clientInfoService.cleanRedisClientInfoChannel();
        }catch (Exception e){
            e.printStackTrace();
        }


    }
}
