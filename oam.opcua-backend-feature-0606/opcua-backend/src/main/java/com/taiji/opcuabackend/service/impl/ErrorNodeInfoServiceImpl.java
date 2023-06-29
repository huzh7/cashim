package com.taiji.opcuabackend.service.impl;

import cn.hutool.core.date.DateUtil;
import com.taiji.opcuabackend.constant.OpcUaConstant;
import com.taiji.opcuabackend.entity.ErrorNodeEnum;
import com.taiji.opcuabackend.entity.ErrorNodeInfo;
import com.taiji.opcuabackend.entity.ErrorNodeInfoStatistics;
import com.taiji.opcuabackend.mapper.ErrorNodeEnumMapper;
import com.taiji.opcuabackend.mapper.ErrorNodeInfoMapper;
import com.taiji.opcuabackend.service.ErrorNodeEnumService;
import com.taiji.opcuabackend.service.ErrorNodeInfoService;
import com.taiji.opcuabackend.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
public class ErrorNodeInfoServiceImpl implements ErrorNodeInfoService {

    @Autowired
    private ErrorNodeInfoMapper errorNodeInfoMapper;

    @Autowired
    private RedisUtil redisUtil;



    @Override
    public void saveErrorNodeInfo(String nodeName, String nodeValue) throws Exception {
        if(nodeName == null || "".equals(nodeName)){
            log.error("需要添加的ErrorNodeType为null或者''");
            throw new IllegalArgumentException("需要添加的ErrorNodeType为null或者''");
        }

        //初始化添加参数
        ErrorNodeInfo errorNodeInfo = new ErrorNodeInfo();

        //切分组合nodename为单层nodename
        String lastNodeName = new String();
        if(nodeName.contains(".")){

            String[] errorNodeArray = nodeName.split("\\.");

            //赋值最后一层节点name
            lastNodeName = errorNodeArray[errorNodeArray.length - 1];
            if(errorNodeArray.length == 1){

                errorNodeInfo.setNode1(errorNodeArray[0]);
            }else if(errorNodeArray.length == 2){

                errorNodeInfo.setNode1(errorNodeArray[0]);
                errorNodeInfo.setNode2(errorNodeArray[1]);
            }else if(errorNodeArray.length == 3){

                errorNodeInfo.setNode1(errorNodeArray[0]);
                errorNodeInfo.setNode2(errorNodeArray[1]);
                errorNodeInfo.setNode3(errorNodeArray[2]);
            }
            else if(errorNodeArray.length == 4){

                errorNodeInfo.setNode1(errorNodeArray[0]);
                errorNodeInfo.setNode2(errorNodeArray[1]);
                errorNodeInfo.setNode3(errorNodeArray[2]);
                errorNodeInfo.setNode4(errorNodeArray[3]);
            }
        }else {
            lastNodeName = nodeName;
            errorNodeInfo.setNode1(nodeName);
        }
        errorNodeInfo.setNodeValue(nodeValue);
        errorNodeInfo.setCreateTime(DateUtil.date());
        errorNodeInfo.setUpdateTime(DateUtil.date());





        //判断是否为异常数据
        if("Heartbeat".equals(lastNodeName)){

            if("".equals(nodeValue) || "0".equals(nodeValue)){ // 0和“”都为心跳得异常状态
                errorNodeInfo.setStatus(0);
            }else {
                errorNodeInfo.setStatus(1);
            }
        }else if("Stauts".equals(lastNodeName)){

            if(Boolean.valueOf(nodeValue) == true){
                errorNodeInfo.setStatus(1);
            }else {
                errorNodeInfo.setStatus(0);
            }

        }else {
            errorNodeInfo.setStatus(1);
        }


        errorNodeInfoMapper.insert(errorNodeInfo);
    }

    @Override
    public List<ErrorNodeInfoStatistics> getStatisticsErrorNodeInfo() throws Exception {
        List<ErrorNodeInfoStatistics> errorNodeInfoStatistics = errorNodeInfoMapper.selectStatisticsErrorNodeInfo();
        return errorNodeInfoStatistics;
    }
}