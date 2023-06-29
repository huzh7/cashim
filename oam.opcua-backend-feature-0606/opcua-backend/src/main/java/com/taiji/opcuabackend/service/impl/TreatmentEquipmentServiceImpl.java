package com.taiji.opcuabackend.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taiji.opcuabackend.entity.CameraInfo;
import com.taiji.opcuabackend.entity.TreatmentEquipment;
import com.taiji.opcuabackend.mapper.CameraInfoMapper;
import com.taiji.opcuabackend.mapper.TreatmentEquipmentMapper;
import com.taiji.opcuabackend.service.CameraInfoService;
import com.taiji.opcuabackend.service.TreatmentEquipmentService;
import com.taiji.opcuabackend.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
public class TreatmentEquipmentServiceImpl implements TreatmentEquipmentService {

    @Autowired
    private TreatmentEquipmentMapper treatmentEquipmentMapper;

    @Autowired
    private RedisUtil redisUtil;
    @Override
    public List<TreatmentEquipment> getTreatmentEquipment(TreatmentEquipment treatmentEquipment) throws Exception {

        //初始化返回对象
        List<TreatmentEquipment> treatmentEquipmentList = new ArrayList<>();

        if(treatmentEquipment == null){
            treatmentEquipmentList = treatmentEquipmentMapper.selectByMap(null);
        }else {
            //条件查询
            LambdaQueryWrapper<TreatmentEquipment> queryWrapper = new LambdaQueryWrapper<>();

            if(treatmentEquipment.getEquipment() != null){
                queryWrapper.eq(TreatmentEquipment::getEquipment, treatmentEquipment.getEquipment());
            }
            if(treatmentEquipment.getTreatmentRoom() != null){
                queryWrapper.eq(TreatmentEquipment::getTreatmentRoom, treatmentEquipment.getTreatmentRoom());
            }
            if(treatmentEquipment.getNodeName() != null){
                queryWrapper.eq(TreatmentEquipment::getNodeName, treatmentEquipment.getNodeName());
            }
            treatmentEquipmentList = treatmentEquipmentMapper.selectList(queryWrapper);
        }

        for(TreatmentEquipment treatmentEquipmentResult : treatmentEquipmentList){
            String equipmentStatus = redisUtil.get(treatmentEquipmentResult.getNodeName());
            if(equipmentStatus != null){
                JSONObject equipmentStatusJsonStr = JSONObject.parseObject(equipmentStatus);
                treatmentEquipmentResult.setStatus(equipmentStatusJsonStr.get("initialValue").toString());

            }else {
                treatmentEquipmentResult.setStatus(null);

            }
        }

        return treatmentEquipmentList;
    }
}