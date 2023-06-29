package com.taiji.opcuabackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taiji.opcuabackend.config.NettyConfig;
import com.taiji.opcuabackend.constant.OpcUaConstant;
import com.taiji.opcuabackend.entity.CameraInfo;
import com.taiji.opcuabackend.entity.ClientInfo;
import com.taiji.opcuabackend.mapper.CameraInfoMapper;
import com.taiji.opcuabackend.mapper.ClientInfoMapper;
import com.taiji.opcuabackend.service.CameraInfoService;
import com.taiji.opcuabackend.service.ClientInfoService;
import com.taiji.opcuabackend.util.PingUtil;
import com.taiji.opcuabackend.util.RedisUtil;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class CameraInfoServiceImpl implements CameraInfoService {

    @Autowired
    private CameraInfoMapper cameraInfoMapper;

    @Override
    public List<CameraInfo> getCameraInfo(CameraInfo cameraInfo) throws Exception {

        if(cameraInfo == null){
            throw new Exception("cameraInfo参数为空！");
        }
        HashMap<String, Object> param = new HashMap<>();

        if(cameraInfo.getIpAddr() != null){
            param.put("ip_addr", cameraInfo.getIpAddr());
        }

        List<CameraInfo> ciList = cameraInfoMapper.selectByMap(param);
        return ciList;

    }

    @Override
    public List<CameraInfo> getCameraInfoByPage(int page, int size) throws Exception {

        // 使用Mybatis-plus分页插件查询cameraInfo
        Page<CameraInfo> rowPage = new Page(page, size);

        //queryWrapper组装查询where条件
        LambdaQueryWrapper<CameraInfo> queryWrapper = new LambdaQueryWrapper<>();
        rowPage = cameraInfoMapper.selectPage(rowPage, queryWrapper);
        return rowPage.getRecords();
    }

    @Override
    public Double getCamerasHealthRate() throws Exception {

        // queryWrapper组装查询count条件
        LambdaQueryWrapper<CameraInfo> queryWrapperNormal = new LambdaQueryWrapper<>();
        queryWrapperNormal.eq(CameraInfo::getStatus, OpcUaConstant.CAMERA_STATUS_NORMAL);
        double normalCount = cameraInfoMapper.selectCount(queryWrapperNormal);

        LambdaQueryWrapper<CameraInfo> queryWrapperOffline = new LambdaQueryWrapper<>();
        queryWrapperOffline.eq(CameraInfo::getStatus, OpcUaConstant.CAMERA_STATUS_OFFLINE);
        double OfflineCount = cameraInfoMapper.selectCount(queryWrapperOffline);


        return normalCount / (normalCount + OfflineCount);
    }

    @Override
    public void updateCameraStatus() throws Exception {

        // 获取所有摄像头ip
        List<CameraInfo> cameraInfoList = cameraInfoMapper.selectList(null);

        log.info("ping摄像头ip是否可用，如果不可用则更新数据库");

        // 遍历所有摄像头ip
        for(CameraInfo cameraInfo : cameraInfoList){
            String ipAddr = cameraInfo.getIpAddr();
            // ping摄像头ip
            boolean isReachable = PingUtil.isReachable(ipAddr);
            // 如果不可达，则更新数据库
            if(!isReachable){
                cameraInfo.setStatus(OpcUaConstant.CAMERA_STATUS_OFFLINE);
                cameraInfoMapper.updateById(cameraInfo);
            }
        }
    }


}