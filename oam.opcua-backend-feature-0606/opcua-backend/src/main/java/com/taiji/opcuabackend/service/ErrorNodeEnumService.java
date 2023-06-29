package com.taiji.opcuabackend.service;

/**
 * 错误节点类型
 */
public interface ErrorNodeEnumService  {

    /**
     * 启动时，加载生效的错误节点类型到reids中
     *
     */
    public void loadErrorNodeEnumToRedis() throws Exception;

    /**
     * 添加ErrorNodeEnum
     *
     */
    public void saveErrorNodeEnum(String errorNodeType) throws Exception;
}
