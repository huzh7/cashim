package com.taiji.opcuabackend.service;

import com.taiji.opcuabackend.entity.AjaxResult;
import com.taiji.opcuabackend.entity.ClientInfo;

import java.util.List;

public interface FFmpegHandlerService {

    /**
     * 海康摄像头拉流后，rtsp转为rtmp流，推流到nginx流媒体服务器
     */
    public AjaxResult pushHcRTMP(String ipAddr, String appName);

    /**
     * 海康摄像头拉流后，rtsp转为hls流，推流到nginx流媒体服务器
     */
    public AjaxResult pushHcHLS(String ipAddr, String appName);

    /**
     *判断是否有推流任务,有则停止
     */
    public AjaxResult stopPushTask(String appName);

}
