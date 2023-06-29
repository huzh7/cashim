package com.taiji.opcuabackend.controller;

import com.taiji.opcuabackend.config.HKStreamConfig;
import com.taiji.opcuabackend.entity.AjaxResult;
import com.taiji.opcuabackend.service.CameraInfoService;
import com.taiji.opcuabackend.service.FFmpegHandlerService;
import com.taiji.opcuabackend.util.FFmpegHandlerUtil;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author sunzb
 */
@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/hk")
public class HKDeviceController {

    @Autowired
    private CameraInfoService cameraInfoService;

    @Autowired
    private HKStreamConfig hkStreamConfig;

    @Autowired
    private FFmpegHandlerUtil ffmpegHandlerUtil;

    @Autowired
    private FFmpegHandlerService ffmpegHandlerService;

    /**
     * 推流到Nginx流媒体服务器并获取RTMP地址
     */

    @ApiOperation(value = "推流到Nginx流媒体服务器（rtsp转为rtmp）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "cameraIP", value = "摄像头IP地址", required = true, dataType = "java.lang.String")
    })
    @PostMapping(value = "/pushAndGetRTMPUrl", produces = "application/json;charset=UTF-8")
    public AjaxResult pushAndGetRTMPUrl(@RequestParam String cameraIP){

        String appName = hkStreamConfig.getCameraAccount() + cameraIP.replace(".","");
        return ffmpegHandlerService.pushHcRTMP(cameraIP, appName);
    }

    @ApiOperation(value = "停止推流到Nginx流媒体服务器")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "appName", value = "推流进程标记名称", required = true, dataType = "java.lang.String")
    })
    @PostMapping(value = "/stopPushHKStream", produces = "application/json;charset=UTF-8")
    public AjaxResult stopPushHKStream(@RequestParam String appName){

        return ffmpegHandlerService.stopPushTask(appName);
    }

    @ApiOperation(value = "推流到Nginx流媒体服务器（rtsp转为hls）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "cameraIP", value = "摄像头IP地址", required = true, dataType = "java.lang.String")
    })
    @PostMapping(value = "/pushAndGetHlsUrl", produces = "application/json;charset=UTF-8")
    public AjaxResult pushAndGetHlsUrl(@RequestParam String cameraIP){

        String ipAddr = cameraIP;
        String appName = hkStreamConfig.getCameraAccount() + cameraIP.replace(".","");
        return ffmpegHandlerService.pushHcHLS(cameraIP, appName);
    }

}