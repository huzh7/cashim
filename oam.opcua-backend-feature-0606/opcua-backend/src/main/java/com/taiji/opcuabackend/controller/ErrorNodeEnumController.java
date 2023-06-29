package com.taiji.opcuabackend.controller;

import com.taiji.opcuabackend.entity.AjaxResult;
import com.taiji.opcuabackend.service.ClientInfoService;
import com.taiji.opcuabackend.service.ErrorNodeEnumService;
import com.taiji.opcuabackend.service.OpcUaSubscriptionService;
import com.taiji.opcuabackend.util.RedisUtil;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author sunzb
 */
@Slf4j
@RestController
@RequestMapping("/errorNodeEnum")
public class ErrorNodeEnumController {

    @Autowired
    private ErrorNodeEnumService errorNodeEnumService;

    /**
     * 保存ErrorNode
     */
    @ApiOperation(value = "保存ErrorNode")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "nodeType", value = "", required = true, dataType = "java.lang.String")
    })
    @PostMapping(value = "/saveErrorNode", produces = "application/json;charset=UTF-8")
    public AjaxResult saveErrorNode(@RequestParam String nodeType){

        try {

            errorNodeEnumService.saveErrorNodeEnum(nodeType);
            return AjaxResult.success("success");
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error(e.toString());
        }

    }

    /**
     * 重新加载ErrorNode到redis
     */
    @PostMapping(value = "/reloadErrorNode", produces = "application/json;charset=UTF-8")
    public AjaxResult reloadErrorNode(){

        try {
            errorNodeEnumService.loadErrorNodeEnumToRedis();
            return AjaxResult.success("success");
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error(e.toString());
        }

    }
}