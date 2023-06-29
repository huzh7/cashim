package com.taiji.opcuabackend.service.impl;

import cn.hutool.core.date.DateUtil;
import com.taiji.opcuabackend.constant.OpcUaConstant;
import com.taiji.opcuabackend.entity.ErrorNodeEnum;
import com.taiji.opcuabackend.mapper.ErrorNodeEnumMapper;
import com.taiji.opcuabackend.service.ErrorNodeEnumService;
import com.taiji.opcuabackend.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
public class ErrorNodeEnumServiceImpl implements ErrorNodeEnumService {

    @Autowired
    private ErrorNodeEnumMapper errorNodeEnumMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public void loadErrorNodeEnumToRedis() throws Exception {

        //初始化参数
        HashMap<String, Object> param = new HashMap<>();
        param.put("status", 1);
        List<ErrorNodeEnum> errorNodeEnumList = errorNodeEnumMapper.selectByMap(param);

        //将errorNodeEnumList放入redis
        if(errorNodeEnumList != null && errorNodeEnumList.size() != 0){
            String[] errorNodeEnumArray = new String[errorNodeEnumList.size()];
            for (int i = 0; i < errorNodeEnumList.size(); i++) {
                errorNodeEnumArray[i] = errorNodeEnumList.get(i).getNodeType();
            }
            redisUtil.addSet(OpcUaConstant.ERROR_NODES, errorNodeEnumArray);
        }
    }

    @Override
    public void saveErrorNodeEnum(String errorNodeType) throws Exception {

        if(errorNodeType == null || "".equals(errorNodeType)){
            log.error("需要添加的ErrorNodeType为null或者''");
            throw new IllegalArgumentException("需要添加的ErrorNodeType为null或者''");
        }

        //初始化添加参数
        ErrorNodeEnum errorNodeEnum = new ErrorNodeEnum();

        errorNodeEnum.setNodeType(errorNodeType);
        errorNodeEnum.setCreateTime(new Date());
        errorNodeEnum.setUpdateTime(new Date());
        errorNodeEnum.setStatus(1);

        errorNodeEnumMapper.insert(errorNodeEnum);

    }
}