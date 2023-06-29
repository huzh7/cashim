package com.taiji.opcuabackend.service;

import com.taiji.opcuabackend.entity.ErrorNodeInfoStatistics;

import java.util.List;

/**
 * 错误节点类型
 */
public interface ErrorNodeInfoService {

    /**
     * 添加ErrorNodeInfo到数据库
     * @param nodeName
     * @param nodeValue
     * @throws Exception
     */
    public void saveErrorNodeInfo(String nodeName, String nodeValue) throws Exception;


    /**
     * 获取统计数据top10
     * @throws Exception
     */
    public List<ErrorNodeInfoStatistics> getStatisticsErrorNodeInfo() throws Exception;
}
