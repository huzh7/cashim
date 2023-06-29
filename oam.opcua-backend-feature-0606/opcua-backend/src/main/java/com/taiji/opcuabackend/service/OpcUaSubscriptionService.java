package com.taiji.opcuabackend.service;

import cn.hutool.json.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.taiji.opcuabackend.entity.AjaxResult;
import com.taiji.opcuabackend.lisenter.OpcUaSubscriptionListener;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface OpcUaSubscriptionService {

    /**
     * 订阅opuca所有节点
     * @throws Exception
     */
    public void opcServerItemSubscription() throws Exception;

    /**
     * 重新订阅opuca所有节点
     */
    public void opcServerItemReSubscription(OpcUaClient client, List<NodeId> nodeIdList, OpcUaSubscriptionListener opcUaSubscriptionListener) throws Exception;

    /**
     * 客户端从opcua-backedn服务端订阅节点
     */
    public HashMap<String, String> nodeItemClientSubscription(String userToken, JSONArray nodesArray, int operationType) throws Exception;

    /**
     * 客户端读取节点的最新值
     */
    public AjaxResult nodeItemClientRead(String userToken, List<String> nodesArray) throws Exception;

    /**
     * 客户端写入Opcua节点的值
     */
    public AjaxResult nodeItemClientWrite(String userToken, JSONObject nodesValue) throws Exception;
}
