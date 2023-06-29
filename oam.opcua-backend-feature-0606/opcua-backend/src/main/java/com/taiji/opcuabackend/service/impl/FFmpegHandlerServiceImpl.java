package com.taiji.opcuabackend.service.impl;

import com.taiji.opcuabackend.config.HKStreamConfig;
import com.taiji.opcuabackend.config.NettyConfig;
import com.taiji.opcuabackend.constant.OpcUaConstant;
import com.taiji.opcuabackend.entity.AjaxResult;
import com.taiji.opcuabackend.entity.CameraInfo;
import com.taiji.opcuabackend.entity.ClientInfo;
import com.taiji.opcuabackend.mapper.CameraInfoMapper;
import com.taiji.opcuabackend.mapper.ClientInfoMapper;
import com.taiji.opcuabackend.service.ClientInfoService;
import com.taiji.opcuabackend.service.FFmpegHandlerService;
import com.taiji.opcuabackend.util.FFmpegHandlerUtil;
import com.taiji.opcuabackend.util.RedisUtil;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class FFmpegHandlerServiceImpl implements FFmpegHandlerService {

    @Autowired
    HKStreamConfig hkStreamConfig;

    @Autowired
    CameraInfoMapper cameraInfoMapper;

    @Autowired
    FFmpegHandlerUtil ffmpegHandlerUtil;

    private static List<String> appNameList = new ArrayList<>();
    @Override
    public AjaxResult pushHcRTMP(String ipAddr, String appName)  {

        //判断appName是否已经存在
        if (appNameList.contains(appName)) {
            //拼写rtmp流地址返回值
            StringBuffer rtmpUrl = new StringBuffer("rtmp://");
            rtmpUrl.append(hkStreamConfig.getNgxinRtmpIP());
            rtmpUrl.append(":").append(hkStreamConfig.getNgxinRtmpPort());
            rtmpUrl.append("/live/").append(appName);

            AjaxResult ajaxExistResult = AjaxResult.success();
            ajaxExistResult.put("rtmpUrl", rtmpUrl.toString());
            ajaxExistResult.put("appName", appName);
            ajaxExistResult.put("timestamp", System.currentTimeMillis());
            return AjaxResult.error("该摄像头已经推流");
        }
        AjaxResult ajaxResult = null;

        //根据ipAddr获取摄像头账号密码以及rtsp地址
        Map<String, Object> param = new HashMap<>();
        param.put("ip_addr", ipAddr);
        List<CameraInfo> ciList = cameraInfoMapper.selectByMap(param);
        if (ciList.size() == 0) {
            return AjaxResult.error("该摄像头不存在");
        }
        CameraInfo cameraInfo = ciList.get(0);

        //拼写ffmpeg转码、推流命令
        StringBuffer command = new StringBuffer("ffmpeg -rtsp_transport tcp -i  ");
        command.append(" rtsp://").append(hkStreamConfig.getCameraAccount());
        command.append(":").append(hkStreamConfig.getCameraPassword());
        command.append("@").append(cameraInfo.getIpAddr());
        command.append(":").append(hkStreamConfig.getStreamPort());
        command.append("/h264/ch1/main/av_stream -f flv -r 60 -g 60 -s 1920x1080 -an rtmp://");
        command.append(hkStreamConfig.getNgxinRtmpIP());
        command.append(":").append(hkStreamConfig.getNgxinRtmpPort());
        command.append("/live/").append(appName);

        //拼写rtmp流地址返回值
        StringBuffer rtmpUrl = new StringBuffer("rtmp://");
        rtmpUrl.append(hkStreamConfig.getNgxinRtmpIP());
        rtmpUrl.append(":").append(hkStreamConfig.getNgxinRtmpPort());
        rtmpUrl.append("/live/").append(appName);

        try {
            ffmpegHandlerUtil.startThread(command.toString(), appName);

            //将appName存入appNameList
            appNameList.add(appName);

            ajaxResult = AjaxResult.success();
            ajaxResult.put("rtmpUrl", rtmpUrl.toString());
            ajaxResult.put("appName", appName);
            ajaxResult.put("timestamp", System.currentTimeMillis());

        } catch (Exception e) {
            return AjaxResult.error("该摄像头不存在");
        }
        return ajaxResult;
    }

    @Override
    public AjaxResult pushHcHLS(String ipAddr, String appName) {
        AjaxResult ajaxResult = null;

        //根据ipAddr获取摄像头账号密码以及rtsp地址
        Map<String, Object> param = new HashMap<>();
        param.put("ip_addr", ipAddr);
        List<CameraInfo> ciList = cameraInfoMapper.selectByMap(param);
        if (ciList.size() == 0) {
            return AjaxResult.error("该摄像头不存在");
        }
        CameraInfo cameraInfo = ciList.get(0);

        //判断appName是否已经存在
        if (appNameList.contains(appName)) {
            //拼写hls流地址返回值
            StringBuffer hlsUrl = new StringBuffer("http://");
            hlsUrl.append(hkStreamConfig.getNgxinRtmpIP());
            hlsUrl.append(":").append("19888");
            hlsUrl.append("/hls/").append(appName).append(".m3u8");

            AjaxResult ajaxExistResult = AjaxResult.success();
            ajaxExistResult.put("hlsUrl", hlsUrl.toString());
            ajaxExistResult.put("appName", appName);
            ajaxExistResult.put("timestamp", System.currentTimeMillis());
            return ajaxExistResult;
        }

        //拼写ffmpeg转码、推流命令
        StringBuffer command = new StringBuffer("ffmpeg -rtsp_transport tcp -i  ");
        command.append(" rtsp://").append(hkStreamConfig.getCameraAccount());
        command.append(":").append(hkStreamConfig.getCameraPassword());
        command.append("@").append(cameraInfo.getIpAddr());
        command.append(":").append(hkStreamConfig.getStreamPort());
        command.append("/h264/ch1/main/av_stream -vcodec libx264 -acodec aac -f flv rtmp://");
        command.append(hkStreamConfig.getNgxinRtmpIP());
        command.append(":").append(hkStreamConfig.getNgxinRtmpPort());
        command.append("/hls/").append(appName);

        //拼写rtmp流地址返回值
        StringBuffer hlsUrl = new StringBuffer("http://");
        hlsUrl.append(hkStreamConfig.getNgxinRtmpIP());
        hlsUrl.append(":").append("19888");
        hlsUrl.append("/hls/").append(appName).append(".m3u8");

        try {
            ffmpegHandlerUtil.startThread(command.toString(), appName);

            //将appName存入appNameList
            appNameList.add(appName);

            ajaxResult = AjaxResult.success();
            ajaxResult.put("hlsUrl", hlsUrl.toString());
            ajaxResult.put("appName", appName);
            ajaxResult.put("timestamp", System.currentTimeMillis());

        } catch (Exception e) {
            return AjaxResult.error("该摄像头不存在");
        }
        return ajaxResult;
    }

    @Override
    public AjaxResult stopPushTask(String appName) {

        //根据appName获取ffmpeg进程
        Boolean isAlive = ffmpegHandlerUtil.isAlive(appName);

        if(appNameList.contains(appName) || isAlive){
            if (appNameList.contains(appName)){
                appNameList.remove(appName);
            }
            if(isAlive){
                ffmpegHandlerUtil.stopThread(appName);
            }
        }else {
            return AjaxResult.error("该摄像头未推流");
        }
        return AjaxResult.success("已经停止摄像头推流");
    }
}