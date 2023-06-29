package com.taiji.opcuabackend.client;

import com.alibaba.fastjson.JSONObject;
import com.taiji.opcuabackend.config.OpcUaConfig;
import com.taiji.opcuabackend.constant.OpcUaConstant;
import com.taiji.opcuabackend.entity.OpcUaItemDataView;
import com.taiji.opcuabackend.lisenter.OpcUaSubscriptionListener;
import com.taiji.opcuabackend.util.Dom4jUtil;
import com.taiji.opcuabackend.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig;
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
import org.eclipse.milo.opcua.sdk.client.nodes.UaNode;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedDataItem;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedSubscription;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoredItemCreateRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoringParameters;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.collect.Lists.newArrayList;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

@Slf4j
@Component
public class UaClient {

    public static OpcUaClient opcUaClient;

    private static AtomicInteger atomic = new AtomicInteger(1);

    private static List<NodeId> parentUaNodeList = new ArrayList<NodeId>();

    private static List<NodeId> opcUaAllNodeList = new ArrayList<NodeId>();

    @Autowired
    private OpcUaConfig opcUaConfig;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private Dom4jUtil dom4jUtil;

    @Autowired
    private OpcUaSubscriptionListener opcUaSubscriptionListener;

    @PostConstruct
    public void createClient() {
        try {
            Path securityTempDir = Paths.get(System.getProperty("java.io.tmpdir"), "security");
            Files.createDirectories(securityTempDir);
            if (!Files.exists(securityTempDir)) {
                throw new Exception("没有创建安全目录: " + securityTempDir);
            }
            log.info("安全目录: {}", securityTempDir.toAbsolutePath());

            //加载秘钥
            KeyStoreLoader loader = new KeyStoreLoader().load(securityTempDir);

            //安全策略 None、Basic256、Basic128Rsa15、Basic256Sha256
            SecurityPolicy securityPolicy = SecurityPolicy.None;

            List<EndpointDescription> endpoints;

            try {
                log.info(opcUaConfig.getEndpointUrl());
                endpoints = DiscoveryClient.getEndpoints(opcUaConfig.getEndpointUrl()).get();
            } catch (Throwable ex) {
                // 发现服务
                String discoveryUrl = opcUaConfig.getEndpointUrl();

                if (!discoveryUrl.endsWith("/")) {
                    discoveryUrl += "/";
                }
//                discoveryUrl += "discovery";

                log.info("开始连接 URL: {}", discoveryUrl);
                endpoints = DiscoveryClient.getEndpoints(discoveryUrl).get();
            }
            EndpointDescription endpoint = endpoints.stream()
                    .filter(e -> e.getSecurityPolicyUri().equals(securityPolicy.getUri()))
                    .filter(opcUaConfig.endpointFilter())
                    .findFirst()
                    .orElseThrow(() -> new Exception("没有连接上端点"));

            log.info("使用端点: {} [{}/{}]", endpoint.getEndpointUrl(), securityPolicy, endpoint.getSecurityMode());

            OpcUaClientConfig config = OpcUaClientConfig.builder()
                    .setApplicationName(LocalizedText.english("eclipse milo opc-ua client"))
                    .setApplicationUri("urn:eclipse:milo:examples:client")
                    .setCertificate(loader.getClientCertificate())
                    .setKeyPair(loader.getClientKeyPair())
                    .setEndpoint(endpoint)
                    //根据匿名验证和第三个用户名验证方式设置传入对象 AnonymousProvider（匿名方式）UsernameProvider（账户密码）
                    //new UsernameProvider("admin","123456")
                    .setIdentityProvider(new AnonymousProvider())
                    .setRequestTimeout(uint(5000))
                    .build();
            opcUaClient = OpcUaClient.create(config);
        } catch (Exception e) {
            log.error("创建客户端失败" + e.getMessage());
        }
    }

    /**
     * 遍历树形节点
     * @throws Exception
     */
    public List<NodeId> browseNode (OpcUaClient uaClient)  throws Exception {
        log.info("查看当前 Ua 节点");
        List<UaNode> uaNodeList = new ArrayList<UaNode>();

        //连接服务端

        uaNodeList = (List<UaNode>)uaClient.getAddressSpace().browseNodes(Identifiers.ObjectsFolder);
        for (UaNode nd : uaNodeList) {
            //排除系统行性节点，这些系统性节点名称一般都是以"_"开头，NodeId的namespaceIndex为2的，一般为业务Id
//            if (Objects.requireNonNull(nd.getBrowseName().getName()).contains("_") ||
//                    nd.getNodeId().getNamespaceIndex().intValue() != 2) {
//                continue;
//            }
            parentUaNodeList.add(nd.getNodeId());
            //获取当前节点的子节点
            browseNode(uaClient, nd);
        }
        return parentUaNodeList;
    }

    /**
     * 遍历树形节点
     *
     * @param uaNode 节点
     * @throws Exception
     */
    public void browseNode (OpcUaClient uaClient, UaNode uaNode)  throws Exception {
        List<UaNode> uaNodeList;
        if (uaNode == null) {

            uaNodeList = (List<UaNode>)uaClient.getAddressSpace().browseNodes(Identifiers.ObjectsFolder);
        } else {

            uaNodeList = (List<UaNode>)uaClient.getAddressSpace().browseNodes(uaNode);
        }
        for (UaNode nd : uaNodeList) {
            //排除系统行性节点，这些系统性节点名称一般都是以"_"开头，NodeId的namespaceIndex为2的，一般为业务Id
//            if (Objects.requireNonNull(nd.getBrowseName().getName()).contains("_") ||
//                    nd.getNodeId().getNamespaceIndex().intValue() != 2) {
//                continue;
//            }
            parentUaNodeList.add(nd.getNodeId());
            //递归查找当前节点下的节点
            browseNode(uaClient, nd);
        }
    }

    /**
     *
     * 读取节点数据
     * @param nodeId
     * @throws Exception
     */
    public void readNode(NodeId nodeId) throws Exception {

        if(nodeId == null){
            log.error("---传入节点参数为null，请确认……");
            throw new IllegalArgumentException("---传入节点参数为null，请确认……");
        }
        //读取节点数据
        DataValue dataValue = opcUaClient.readValue(0.0, TimestampsToReturn.Neither, nodeId).get();
        //标识符
        String nodeIdentifier = String.valueOf(nodeId.getIdentifier());
        log.info(nodeIdentifier + ": " + String.valueOf(dataValue.getValue().getValue()));
    }

    /**
     * 写入节点数据
     *
     * @param identifier
     * @param value
     * @throws Exception
     */
    public boolean writeNodeValue(String identifier, Object value){
        //节点
        NodeId nodeId = new NodeId(2, identifier);
        //创建数据对象,此处的数据对象一定要定义类型，不然会出现类型错误，导致无法写入
        DataValue newValue = new DataValue(new Variant(value), null, null);
        //写入节点数据
        StatusCode statusCode = opcUaClient.writeValue(nodeId, newValue).join();
        System.out.println("结果：" + statusCode.isGood());
        return statusCode.isGood();
    }

    /**
     *  订阅(单个OpcUa节点)
     *
     * @param nodeId 需要查询的节点
     * @throws Exception
     */

    public void subscribe(NodeId nodeId) throws Exception {

        Map<String, OpcUaItemDataView> opcUaItemDataViewMap = dom4jUtil.getRecursionItemData();

        //创建发布间隔的订阅对象
        opcUaClient.getSubscriptionManager()
                .createSubscription(OpcUaConstant.SUBSCRIPTION_TIME)
                .thenAccept(t -> {
                    ReadValueId readValueId = new ReadValueId(nodeId, AttributeId.Value.uid(), null, null);
                    //创建监控的参数
                    MonitoringParameters parameters = new MonitoringParameters(UInteger.valueOf(atomic.getAndIncrement()), OpcUaConstant.SUBSCRIPTION_TIME, null, UInteger.valueOf(10), true);
                    //创建监控项请求
                    //该请求最后用于创建订阅。
                    MonitoredItemCreateRequest request = new MonitoredItemCreateRequest(readValueId, MonitoringMode.Reporting, parameters);
                    List<MonitoredItemCreateRequest> requests = new ArrayList<>();
                    requests.add(request);
                    //创建监控项，并且注册变量值改变时候的回调函数。
                    t.createMonitoredItems(
                            TimestampsToReturn.Both,
                            requests,
                            (item, id) -> item.setValueConsumer((it, val) -> {
                                log.info("nodeid :" + it.getReadValueId().getNodeId().getIdentifier().toString() + ", value :" + val.getValue().getValue());
                                if(it.getReadValueId().getNodeId().getIdentifier() != null){
                                    OpcUaItemDataView  opcUaItemDataView = opcUaItemDataViewMap.get(it.getReadValueId().getNodeId().getIdentifier().toString());
                                    if(opcUaItemDataView != null){
                                        opcUaItemDataView.setNodeValue(val.getValue().getValue().toString());
                                        redisUtil.set(it.getReadValueId().getNodeId().toString(), JSONObject.toJSON(opcUaItemDataView).toString());
                                    }
                                }
                            })
                    );
                }).get();
        //持续订阅
        Thread.sleep(Long.MAX_VALUE);
    }

    /**
     * 批量订阅
     *
     * @param nodeIdList@param nodeIdList 需要订阅的节点列表
     * @throws Exception
     */
    public void managedSubscriptionEvent(List<NodeId> nodeIdList) throws Exception {
        final CountDownLatch eventLatch = new CountDownLatch(1);

        //opcUaClient添加订阅监听OpcUaSubscriptionListener
        opcUaSubscriptionListener.setOpcUaSubscriptionListener(opcUaClient, nodeIdList);
        opcUaClient.getSubscriptionManager().addSubscriptionListener(opcUaSubscriptionListener);

        //处理订阅业务
            //赋值给全局变量，断开重连时可用
        opcUaAllNodeList = nodeIdList;
            //批量订阅
        batchHandlerNode(nodeIdList);

        //持续监听
        eventLatch.await();
    }

    /**
     * 处理批量订阅业务
     *
     * @param nodeIdList
     * @throws Exception
     */
    public void handlerNode(List<NodeId> nodeIdList) throws Exception{

        Map<String, OpcUaItemDataView> opcUaItemDataViewMap = dom4jUtil.getRecursionItemData();

        try {
            if (nodeIdList == null || nodeIdList.size() == 0){
                log.error("---传入节点参数为null，请确认……");
                throw new IllegalArgumentException("---传入节点参数为null，请确认……");
            }
            //创建订阅
            ManagedSubscription subscription = ManagedSubscription.create(opcUaClient);

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

    /**
     * 处理批量订阅业务2
     *
     * @param nodeIdList
     * @throws Exception
     */
    public void batchHandlerNode(List<NodeId> nodeIdList) throws Exception{

        Map<String, OpcUaItemDataView> opcUaItemDataViewMap = dom4jUtil.getRecursionItemData();

        try {
            if (nodeIdList == null || nodeIdList.size() == 0){
                log.error("---传入节点参数为null，请确认……");
                throw new IllegalArgumentException("---传入节点参数为null，请确认……");
            }
            //创建订阅
            UaSubscription subscription = opcUaClient.getSubscriptionManager().createSubscription(OpcUaConstant.SUBSCRIPTION_TIME).get();

            for (int i = 0; i < nodeIdList.size(); i++) {
                NodeId nodeId = nodeIdList.get(i);
                //创建订阅的变量
                ReadValueId readValueId = new ReadValueId(nodeId, AttributeId.Value.uid(), null, null);
                //创建监控的参数
                MonitoringParameters parameters = new MonitoringParameters(
                        uint(1 + i),  // 为了保证唯一性，否则key值一致
                        0.0,     // sampling interval
                        null,       // filter, null means use default
                        uint(10),   // queue size
                        true        // discard oldest
                );

                MonitoredItemCreateRequest request = new MonitoredItemCreateRequest(readValueId, MonitoringMode.Reporting, parameters);
                //创建监控项，并且注册变量值改变时候的回调函数。
                List<UaMonitoredItem> items = subscription.createMonitoredItems(
                        TimestampsToReturn.Both,
                        newArrayList(request),
                        (item, id) -> {
                            item.setValueConsumer((is, value) -> {

                                //如果返回的节点，存在xml解析后的节点对应关系,则进行处理
                                if(item.getReadValueId().getNodeId() != null && item.getReadValueId().getNodeId().getIdentifier() != null){
                                    String nodeName = item.getReadValueId().getNodeId().getIdentifier().toString();

                                    OpcUaItemDataView opcUaItemDataView = opcUaItemDataViewMap.get(nodeName);
                                    if(opcUaItemDataView != null){//如果返回的节点，存在xml解析后的节点对应关系

                                        //批量订阅后，将最新的opcua的节点value保存到redis中
                                        if ( value.getValue().getValue() == null ){
                                            redisUtil.set(nodeName, "null");
                                        }else if (value.getValue().getValue() instanceof Boolean[]){

                                            //如果存在key，则删除后重新进行添加
                                            if(redisUtil.exists(nodeName)){
                                                redisUtil.del(nodeName);
                                            }
                                            String resutlStr = Arrays.toString((Boolean[])value.getValue().getValue());
                                            opcUaItemDataView.setNodeValue(resutlStr);
                                            redisUtil.set(nodeName, JSONObject.toJSON(opcUaItemDataView).toString());
                                        }else {
                                            opcUaItemDataView.setNodeValue(value.getValue().getValue().toString());
                                            redisUtil.set(nodeName, JSONObject.toJSON(opcUaItemDataView).toString());
                                        }
                                    }
                                }
                            });
                        }).get();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
