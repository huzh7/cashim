package com.taiji.opcuabackend.constant;

public class OpcUaConstant
{

    /**
     * 已连接客户端token列表前缀
     */
    public static final String CLIENT_TOKEN_LIST_KEY = "clientTokenList";

    /**
     * 订阅节点的列表前缀
     */
    public static final String SUBSCRIPTION_NODES_REDIS_KEY = "subscriptionNodes";

    /**
     * 订阅节点的列表前缀
     */
    public static final String ERROR_NODES = "errorNodeEnumArray";

    /**
     * 订阅OpcUaServer节点的时间间隔
     */
    public static final double SUBSCRIPTION_TIME = 500.0;

    /**
     * websocket返回订阅节点内容的分页大小
     */
    public static final double SUBSCRIPTION_PAGE_SIZE = 50;

    /**
     * websocket节点订阅、减小订阅节点、增加订阅节点的操作类型 0：订阅软件节点 1：减小订阅节点 2：增加订阅节点
     */
    public static final int SUBSCRIPTION_OPERATION_TYPE_SUB = 0;
    public static final int SUBSCRIPTION_OPERATION_TYPE_MINUS = 1;
    public static final int SUBSCRIPTION_OPERATION_TYPE_ADD = 2;

    /**
     * cameraInfo表中status字段的值 0: 正常 1: 离线
     */
    public static final int CAMERA_STATUS_NORMAL = 0;
    public static final int CAMERA_STATUS_OFFLINE = 1;

    /**
     * clientInfo表中status字段的值 0: 正常 1: 过期 2: 禁用
     */
    public static final int CLIENT_STATUS_NOMAL = 0;
    public static final int CLIENT_STATUS_EXPIRED = 1;
    public static final int CLIENT_STATUS_DISABLE = 2;
}
