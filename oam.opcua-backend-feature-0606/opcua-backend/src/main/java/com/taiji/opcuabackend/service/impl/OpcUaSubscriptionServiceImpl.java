package com.taiji.opcuabackend.service.impl;

import cn.hutool.json.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.taiji.opcuabackend.client.UaClient;
import com.taiji.opcuabackend.constant.ClientInfoConstant;
import com.taiji.opcuabackend.constant.OpcUaConstant;
import com.taiji.opcuabackend.entity.AjaxResult;
import com.taiji.opcuabackend.entity.ClientInfo;
import com.taiji.opcuabackend.entity.OpcUaItemDataView;
import com.taiji.opcuabackend.lisenter.OpcUaSubscriptionListener;
import com.taiji.opcuabackend.mapper.ClientInfoMapper;
import com.taiji.opcuabackend.service.OpcUaSubscriptionService;
import com.taiji.opcuabackend.util.Dom4jUtil;
import com.taiji.opcuabackend.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.buf.StringUtils;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedDataItem;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedSubscription;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CountDownLatch;

@Slf4j
@Service
public class OpcUaSubscriptionServiceImpl implements OpcUaSubscriptionService {

    @Autowired
    private Dom4jUtil dom4jUtil;

    @Autowired
    private UaClient opcUaClient;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private ClientInfoMapper clientInfoMapper;

    @Override
    public void opcServerItemSubscription() throws Exception {

        Map<String, OpcUaItemDataView> opcUaItemDataViewMap = dom4jUtil.getRecursionItemData();
        if (opcUaItemDataViewMap == null && opcUaItemDataViewMap.size() == 0){
            throw new Exception("XMl节点集合opcUaItemDataViewMap为null，或者无任何元素");
        }

        //订阅节点
        //创建客户端，并对服务端进行连接
        OpcUaClient uaClient = UaClient.opcUaClient;
        if(uaClient != null){
            uaClient.connect().get();
        }else {
            throw new Exception("OpcUaClient为null，无法连接OpcUa Server");
        }

        //获取所有OpcUa节点
        //获取OpcUa服务端所有节点
        List<NodeId> nodeIdList = opcUaClient.browseNode(uaClient);
        log.info("获取到OpcUa Server所有节点数量为：---------------------------" + nodeIdList.size());

        List<NodeId> nodeIdParamList = new ArrayList<>();

        for (NodeId nodeId : nodeIdList) {
            //对比Xml所有节点和NodeId所有节点，并将相同部分节点标记订阅
            if (opcUaItemDataViewMap.get(nodeId.getIdentifier().toString()) != null){
                NodeId nodeIdParam = new NodeId(nodeId.getNamespaceIndex().intValue(), nodeId.getIdentifier().toString());
                nodeIdParamList.add(nodeIdParam);
            }
        }

        //从Opcua服务端订阅所有节点，并将节点的值放入redis中
        opcUaClient.managedSubscriptionEvent(nodeIdParamList);

        uaClient.disconnect();

    }

    @Override
    public void opcServerItemReSubscription(OpcUaClient client, List<NodeId> nodeIdList, OpcUaSubscriptionListener opcUaSubscriptionListener) throws Exception {

        final CountDownLatch eventLatch = new CountDownLatch(1);

        //opcUaClient添加订阅监听OpcUaSubscriptionListener
//        opcUaSubscriptionListener.setOpcUaSubscriptionListener(client, nodeIdList);
//        client.getSubscriptionManager().addSubscriptionListener(opcUaSubscriptionListener);

        //批量订阅
        batchHanderNodes(client, nodeIdList);

        //持续监听
        eventLatch.await();

    }

    //重新批量订阅
    private void batchHanderNodes(OpcUaClient client, List<NodeId> nodeIdList){
        Map<String, OpcUaItemDataView> opcUaItemDataViewMap = dom4jUtil.getRecursionItemData();

        try {
            if (nodeIdList == null || nodeIdList.size() == 0){
                log.error("---传入节点参数为null，请确认……");
                throw new IllegalArgumentException("---传入节点参数为null，请确认……");
            }
            //创建订阅
            ManagedSubscription subscription = ManagedSubscription.create(client);

            //监听
            List<ManagedDataItem> dataItemList = subscription.createDataItems(nodeIdList);
            for (ManagedDataItem managedDataItem : dataItemList) {
                managedDataItem.addDataValueListener((t) -> {

                    if(managedDataItem.getNodeId() != null && managedDataItem.getNodeId().getIdentifier() != null){

                        OpcUaItemDataView opcUaItemDataView = opcUaItemDataViewMap.get(managedDataItem.getNodeId().getIdentifier().toString());
                        if(opcUaItemDataView != null){//如果返回的节点，存在xml解析后的节点对应关系

                            //批量订阅后，将最新的opcua的节点value保存到redis中
                            if ( t.getValue().getValue() == null ){
                                redisUtil.set(managedDataItem.getNodeId().getIdentifier().toString(), "null");
                            }else if (t.getValue().getValue() instanceof Boolean[]){

                                //如果存在key，则删除后重新进行添加
                                if(redisUtil.exists(managedDataItem.getNodeId().getIdentifier().toString())){
                                    redisUtil.del(managedDataItem.getNodeId().getIdentifier().toString());
                                }
                                String resutlStr = Arrays.toString((Boolean[])t.getValue().getValue());
                                opcUaItemDataView.setNodeValue(resutlStr);
                                redisUtil.set(managedDataItem.getNodeId().getIdentifier().toString(), JSONObject.toJSON(opcUaItemDataView).toString());
                            }else {
                                opcUaItemDataView.setNodeValue(t.getValue().getValue().toString());
                                redisUtil.set(managedDataItem.getNodeId().getIdentifier().toString(), JSONObject.toJSON(opcUaItemDataView).toString());
                            }
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public HashMap<String, String> nodeItemClientSubscription(String userToken, JSONArray nodesArray, int operationType) throws Exception {

        //初始化返回结果
        HashMap<String, String> responseMap = new HashMap<>();

        //判断客户端是否传入userToken，通过数据库中注册的用户token进行验证
        ClientInfo ci  = new ClientInfo();
        ci.setToken(userToken);
        List<ClientInfo> clientInfoList = getClientInfo(ci);
        if(userToken == null || clientInfoList == null || clientInfoList.size() ==0) {
            responseMap.put("status", "-1");
            responseMap.put("msg", "token参数有误，或用户令牌不存在！");
            return responseMap;
        }
        ClientInfo resultCi = clientInfoList.get(0);
        if(resultCi.getStatus() == OpcUaConstant.CLIENT_STATUS_EXPIRED) {
            responseMap.put("status", "-2");
            responseMap.put("msg", "token已经过期，请重新获取！");
            return responseMap;
        }
        if(resultCi.getStatus() == OpcUaConstant.CLIENT_STATUS_EXPIRED) {
            responseMap.put("status", "-3");
            responseMap.put("msg", "token对应的客户端已经被禁用，请联系管理员！");
            return responseMap;
        }
        //判断客户端传入的操作类型
        switch (operationType){
            case OpcUaConstant.SUBSCRIPTION_OPERATION_TYPE_SUB:
                //订阅节点
                responseMap = subscriptNodeItem(nodesArray, resultCi);
                break;
            case OpcUaConstant.SUBSCRIPTION_OPERATION_TYPE_MINUS:
                //减少订阅节点
                responseMap = reduceNodeItemSubscription(nodesArray, resultCi);
                break;
            case OpcUaConstant.SUBSCRIPTION_OPERATION_TYPE_ADD:
                //增加订阅节点
                responseMap = addNodeItemSubscription(nodesArray, resultCi);
                break;
            default:
                responseMap.put("status","-1");
                responseMap.put("msg","用户操作类型有误，请确认！");
                break;
        }
        return responseMap;
    }

    @Override
    public AjaxResult nodeItemClientRead(String userToken, List<String> nodesArray) throws Exception {

        AjaxResult ajaxResult = null;

        if(nodesArray == null || nodesArray.size() == 0){
            return AjaxResult.error("传入的节点参数有误，请确认！");
        }

        ClientInfo ci = new ClientInfo();
        ci.setToken(userToken);
        List<ClientInfo> clientInfoList = getClientInfo(ci);
        if(clientInfoList != null && clientInfoList.size() != 0){
            ClientInfo resultCi = clientInfoList.get(0);
            HashMap<String, String> responseMap = new HashMap<>();

            String[] strArray = nodesArray.toArray(new String[nodesArray.size()]);

            List<String> resultStrList = redisUtil.mget(strArray);
            String resultStr =  "[" + StringUtils.join(resultStrList) + "]";

            ajaxResult = AjaxResult.success();
            ajaxResult.put("nodes",resultStr);
        }else {
            return AjaxResult.error("用户token有误，或用户令牌不存在！");
        }
        return ajaxResult;
    }

    @Override
    public AjaxResult nodeItemClientWrite(String userToken, JSONObject nodesValue) throws Exception {

        AjaxResult ajaxResult = null;

        if(nodesValue == null || nodesValue.size() == 0){
            return AjaxResult.error("传入的节点参数有误，请确认！");
        }

        HashMap<String, Boolean> writeStatuMap = new HashMap<>();
        ClientInfo ci = new ClientInfo();
        ci.setToken(userToken);
        List<ClientInfo> clientInfoList = getClientInfo(ci);
        if(clientInfoList != null && clientInfoList.size() != 0){

            //使用lambda表达式对nodesValue进行遍历
            nodesValue.forEach((k,v) -> {

                //从redis查询节点信息,并转为JSONObject
                String nodeItemStr = redisUtil.get(k);
                JSONObject nodeItemJson = JSONObject.parseObject(nodeItemStr);
                String dataType = nodeItemJson.get("datatype").toString();
                //根据节点的数据类型，进行数据类型转换
                if(dataType.equals("string")){
                    try {
                        boolean statusCode = opcUaClient.writeNodeValue(k,v.toString());
                        writeStatuMap.put(k,statusCode);
                    } catch (Exception e) {
                        log.error(v + ": 节点数据类型转换有误，需要string类型");
                    }
                }else if(dataType.equals("uint")){

                    try {
                        boolean statusCode = opcUaClient.writeNodeValue(k,Integer.valueOf(v.toString()).intValue());
                        writeStatuMap.put(k,statusCode);
                    } catch (Exception e) {
                        log.error(v + ": 节点数据类型转换有误，需要uint类型");
                    }
                }else if(dataType.equals("uint16")){

                    try {
                        boolean statusCode = opcUaClient.writeNodeValue(k,Integer.valueOf(v.toString()).intValue());
                        writeStatuMap.put(k,statusCode);
                    } catch (NumberFormatException e) {
                        log.error(v + ": 节点数据类型转换有误，需要uint16类型");
                    }
                }else if(dataType.equals("boolean")){

                    try {
                        boolean statusCode = opcUaClient.writeNodeValue(k,Boolean.valueOf(v.toString()));
                        writeStatuMap.put(k,statusCode);
                    } catch (Exception e) {
                        log.error(v + ": 节点数据类型转换有误，需要boolean类型");
                    }
                }else if(dataType.equals("double")){

                    try {
                        boolean statusCode = opcUaClient.writeNodeValue(k,Double.valueOf(v.toString()));
                        writeStatuMap.put(k,statusCode);
                    } catch (NumberFormatException e) {
                        log.error(v + ": 节点数据类型转换有误，需要double类型");
                    }
                }else if(dataType.equals("boolean array")){

                    try {
                        boolean statusCode = opcUaClient.writeNodeValue(k,com.alibaba.fastjson.JSONArray.parseArray(v.toString(),Boolean.class));
                        writeStatuMap.put(k,statusCode);
                    } catch (Exception e) {
                        log.error(v + ": 节点数据类型转换有误，需要boolean array类型");
                    }
                }else if(dataType.equals("double array")){

                    try {
                        boolean statusCode = opcUaClient.writeNodeValue(k,com.alibaba.fastjson.JSONArray.parseArray(v.toString(),Double.class));
                        writeStatuMap.put(k,statusCode);
                    } catch (Exception e) {
                        log.error(v + ": 节点数据类型转换有误，需要double array类型");
                    }
                }else{
                    log.error(v + ": 节点数据类型有误，请确认！");
                }
            });
        }else {
             ajaxResult = AjaxResult.error("用户token有误，或用户令牌不存在！");
            return ajaxResult;
        }
        ajaxResult = AjaxResult.success();
        ajaxResult.put("writeStatuMap",writeStatuMap);
        return ajaxResult;
    }

    /**
     * 订阅节点
     * @param nodesArray
     * @param ci
     * @return
     */
    private HashMap<String, String> subscriptNodeItem( JSONArray nodesArray, ClientInfo ci) throws Exception {

        //初始化返回结果
        HashMap<String, String> responseMap = new HashMap<>();
        responseMap.put("operationType",String.valueOf(OpcUaConstant.SUBSCRIPTION_OPERATION_TYPE_SUB));

        //判断该用户的socket通道已经建立
        if(ci != null && ci.getIsChannelOn().intValue() == 1){
            responseMap.put("status","-1");
            responseMap.put("msg","该客户端token已经websocket通道已存在！");
            return responseMap;
        }
        // 回复消息
        //获取全部xml文件节点，来获取除value之外的节点其他属性
        Map<String, OpcUaItemDataView> opcUaItemDataViewMap = dom4jUtil.getRecursionItemData();

        //初始化一个客户端将返回值List
        List<OpcUaItemDataView> resultOpcUaItemDataViewList = new ArrayList<>();
        //从redis中取数据返回客户端
        com.alibaba.fastjson.JSONArray nodeJsonArray = new com.alibaba.fastjson.JSONArray();

        //标记该客户端通道建立标识
        ci.setIsChannelOn(ClientInfoConstant.CLIENT_INFO_CHANNEL_EXIST);
        modifyClientInfoChannelStatus(ci);

        String userToken = ci.getToken();
        for (int i = 0; i < nodesArray.size(); i++) {
            String nodeName = (String)nodesArray.get(i);
            String subscriptionNode = new StringBuffer(OpcUaConstant.SUBSCRIPTION_NODES_REDIS_KEY).append("|").append(nodeName).toString();
            //判断redis中是否已经存在订阅节点，如果没有则新建，如果有则取出进行添加操作
            if(redisUtil.exists(subscriptionNode)){

                //判断该redis中的节点是否存在当前客户端token（是否当前客户端订阅该节点）
                if(!redisUtil.isExistSet(subscriptionNode, userToken)){
                    redisUtil.addSet(subscriptionNode,userToken);
                }
            }else {
                redisUtil.addSet(subscriptionNode,userToken);
            }
        }

        List<String> nodesList = nodesArray.toList(String.class);
        String[] strArray = nodesList.toArray(new String[nodesList.size()]);

        List<String> resultStrList = redisUtil.mget(strArray);
        String resultStr =  "[" + StringUtils.join(resultStrList) + "]";
        if(resultStrList != null){ //发送信息到客户端
            responseMap.put("nodes", resultStr);
            responseMap.put("status","0");
            responseMap.put("msg","success");
        }
        return responseMap;
    }

    /**
     * 增加订阅节点
     * @param nodesArray
     * @param ci
     * @return
     */
    private HashMap<String, String> addNodeItemSubscription(JSONArray nodesArray, ClientInfo ci){

        //初始化返回结果
        HashMap<String, String> responseMap = new HashMap<>();
        responseMap.put("operationType",String.valueOf(OpcUaConstant.SUBSCRIPTION_OPERATION_TYPE_ADD));

        if(ci != null && ci.getIsChannelOn().intValue() == 0){

            responseMap.put("status","-1");
            responseMap.put("msg","该客户端尚未建立websocket订阅连接!");
            return responseMap;
        }

        // 回复消息
        String userToken = ci.getToken();
        for (int i = 0; i < nodesArray.size(); i++) {
            String nodeName = (String)nodesArray.get(i);
            String subscriptionNode = new StringBuffer(OpcUaConstant.SUBSCRIPTION_NODES_REDIS_KEY).append("|").append(nodeName).toString();
            //判断redis中是否已经存在订阅节点，如果没有则新建，如果有则取出进行添加操作
            if(redisUtil.exists(subscriptionNode)){

                //判断该redis中的节点是否存在当前客户端token（是否当前客户端订阅该节点）
                if(!redisUtil.isExistSet(subscriptionNode, userToken)){
                    redisUtil.addSet(subscriptionNode,userToken);
                }
            }else {
                redisUtil.addSet(subscriptionNode,userToken);
            }
        }

        //从redis中取数据返回客户端
        List<String> nodesList = nodesArray.toList(String.class);
        String[] strArray = nodesList.toArray(new String[nodesList.size()]);

        List<String> resultStrList = redisUtil.mget(strArray);
        String resultStr =  "[" + StringUtils.join(resultStrList) + "]";
        if(resultStrList != null){ //发送信息到客户端
            responseMap.put("nodes", resultStr);
            responseMap.put("status","0");
            responseMap.put("msg","success");
        }
        return responseMap;
    }

    /**
     * 减小订阅节点
     * @param nodesArray
     * @param ci
     * @return
     */
    private HashMap<String, String> reduceNodeItemSubscription(JSONArray nodesArray, ClientInfo ci){

        //初始化返回结果
        HashMap<String, String> responseMap = new HashMap<>();
        responseMap.put("operationType",String.valueOf(OpcUaConstant.SUBSCRIPTION_OPERATION_TYPE_MINUS));

        String userToken = ci.getToken();
        for (int i = 0; i < nodesArray.size(); i++) {
            String nodeName = (String)nodesArray.get(i);
            String subscriptionNode = new StringBuffer(OpcUaConstant.SUBSCRIPTION_NODES_REDIS_KEY).append("|").append(nodeName).toString();
            //判断redis中是否已经存在订阅节点，如果没有则新建，如果有则取出进行添加操作
            if(redisUtil.exists(subscriptionNode)){

                //判断该redis中的节点是否存在当前客户端token（是否当前客户端订阅该节点）
                log.error("判断该redis中的节点是否存在当前客户端token:" + redisUtil.isExistSet(subscriptionNode, userToken));
                if(redisUtil.isExistSet(subscriptionNode, userToken)){
                    redisUtil.delSet(subscriptionNode,userToken);
                }
            }
            responseMap.put("status","0");
            responseMap.put("msg","success");
        }
        return responseMap;
    }

    /**
     * 获取客户端信息
     * @param clientInfo
     * @return
     * @throws Exception
     */
    private List<ClientInfo> getClientInfo(ClientInfo clientInfo) throws Exception {

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

    /**
     * 修改客户端通道状态
     * @param clientInfo
     * @throws Exception
     */
    private void modifyClientInfoChannelStatus(ClientInfo clientInfo) throws Exception {
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
}
