package com.taiji.opcuabackend.service;

import com.taiji.opcuabackend.entity.CameraInfo;
import com.taiji.opcuabackend.entity.ClientInfo;

import java.util.List;

public interface CameraInfoService {

    /**
     * 查询海康摄像头IP信息
     * @param cameraInfo
     */
    public List<CameraInfo> getCameraInfo(CameraInfo cameraInfo) throws Exception;

    /**
     * 分页查询海康摄像头IP信息
     */
    public List<CameraInfo> getCameraInfoByPage(int page, int size) throws Exception;


    /**
     * 查询摄像头健康度信息
     */
    public Double getCamerasHealthRate() throws Exception;

    /**
     * ping摄像头ip是否可用，如果不可用则更新数据库
     */
    public void updateCameraStatus() throws Exception;
}
