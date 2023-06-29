package com.taiji.opcuabackend.lisenter;

import cn.hutool.core.date.DateTime;
import com.taiji.opcuabackend.service.OpcUaSubscriptionService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscriptionManager;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 自定义订阅监听
 */

@Slf4j
@Component
public class OpcUaSubscriptionListener implements UaSubscriptionManager.SubscriptionListener {

    private OpcUaClient client;

    private List<NodeId> opcUaAllNodeIdList;

    @Autowired
    private OpcUaSubscriptionService opcUaSubscriptionService;

    public void setOpcUaSubscriptionListener(OpcUaClient client, List<NodeId> nodeIdList) {
        this.client = client;
        opcUaAllNodeIdList = nodeIdList;
    }
    @Override
    public void onStatusChanged(UaSubscription subscription, StatusCode status) {
        log.info("onStatusChanged");
    }
    @Override
    public void onPublishFailure(UaException exception) {
        log.info("opcua 服务断开，请检查重连~");
    }
    @Override
    public void onNotificationDataLost(UaSubscription subscription) {
        log.info("onNotificationDataLost");
    }

    /**
     * 重连时 尝试恢复之前的订阅失败时 会调用此方法
     * @param uaSubscription 订阅
     * @param statusCode 状态
     */
    @Override
    public void onSubscriptionTransferFailed(UaSubscription uaSubscription, StatusCode statusCode) {
        //定时重连后，将xml中的节点重新订阅
        log.info("恢复订阅失败 需要重新订阅，开始重新订阅xml文件节点……");
        //在回调方法中重新订阅
        try {
            opcUaSubscriptionService.opcServerItemReSubscription(client, opcUaAllNodeIdList, this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}

