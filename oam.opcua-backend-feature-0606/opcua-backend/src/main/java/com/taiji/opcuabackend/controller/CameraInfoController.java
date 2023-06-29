package com.taiji.opcuabackend.controller;

import com.taiji.opcuabackend.entity.AjaxResult;
import com.taiji.opcuabackend.entity.CameraInfo;
import com.taiji.opcuabackend.service.CameraInfoService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sunzb
 */
@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/cameraInfo")
public class CameraInfoController {

    @Autowired
    private CameraInfoService cameraInfoService;

    /**
     *
     * @param page
     * @param size
     * @param request
     * @return
     */
    @ApiOperation(value = "分页获取所有摄像头信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "开始页", required = true, dataType = "java.lang.Integer"),
            @ApiImplicitParam(name = "size", value = "每页显示数量", required = true, dataType = "java.lang.Integer")
    })
    @GetMapping(value = "/getCamerasInfoListByPage", produces = "application/json;charset=UTF-8")
    public AjaxResult getToken(@RequestParam int page, @RequestParam int size, HttpServletRequest request){

        if (page < 0 || size < 0){
            return AjaxResult.error("page or size is not valid");
        }
        try {
            List<CameraInfo> cameraInfoList = cameraInfoService.getCameraInfoByPage(page,size);
            return AjaxResult.success(cameraInfoList);
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error(e.toString());
        }

    }

    @ApiOperation(value = "获取摄像健康率")
    @GetMapping(value = "/getCamerasHealthRate", produces = "application/json;charset=UTF-8")
    public AjaxResult getCamerasHealthRate(HttpServletRequest request){

        try {
            DecimalFormat df = new DecimalFormat("0.00");
            Double camerasHealthRate = cameraInfoService.getCamerasHealthRate() * 100;
            Map<String, String> map = new HashMap<>();
            map.put("camerasHealthRate", df.format(camerasHealthRate));
            return AjaxResult.success(map);
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error(e.toString());
        }

    }

}