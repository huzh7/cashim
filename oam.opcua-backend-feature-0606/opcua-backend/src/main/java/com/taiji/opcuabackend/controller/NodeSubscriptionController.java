package com.taiji.opcuabackend.controller;

import cn.hutool.json.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.taiji.opcuabackend.entity.AjaxResult;
import com.taiji.opcuabackend.entity.CameraInfo;
import com.taiji.opcuabackend.entity.TreatmentEquipment;
import com.taiji.opcuabackend.service.CameraInfoService;
import com.taiji.opcuabackend.service.OpcUaSubscriptionService;
import com.taiji.opcuabackend.service.TreatmentEquipmentService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @author sunzb
 */
@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/subscription")
public class NodeSubscriptionController {

    @Autowired
    private OpcUaSubscriptionService opcUaSubscriptionService;

    @Autowired
    private TreatmentEquipmentService treatmentEquipmentService;

    /**
     *
     * @param token
     * @param nodes
     * @return
     */
    @ApiOperation(value = "获取最新得节点值")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "用户令牌", required = true, dataType = "java.lang.String"),
            @ApiImplicitParam(name = "nodes", value = "节点数组", required = true, dataType = "java.util.List"),
    })
    @GetMapping(value = "/getNodeValue", produces = "application/json;charset=UTF-8")
    public AjaxResult getNodeValue(@RequestParam String token, @RequestParam List<String> nodes){
        AjaxResult ajaxResult = null;
        try {
            ajaxResult =  opcUaSubscriptionService.nodeItemClientRead(token, nodes);
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error(e.toString());
        }
        return ajaxResult;
    }

    /**
     *
     * @param token
     * @param nodes
     * @param request
     * @return
     */
    @ApiOperation(value = "向opcua节点写入值")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "用户令牌", required = true, dataType = "java.lang.String"),
            @ApiImplicitParam(name = "nodes", value = "节点键值对", required = true, dataType = "java.util.Map"),
    })
    @PostMapping(value = "/writeNodeValue", produces = "application/json;charset=UTF-8")
    public AjaxResult writeNodeValue(@RequestParam String token,@RequestParam Map<String,String> nodes, HttpServletRequest request){

        JSONObject jsonObject = nodes.get("nodes") == null ? null : JSONObject.parseObject(nodes.get("nodes"));
        AjaxResult ajaxResult = null;
        try {
            ajaxResult =  opcUaSubscriptionService.nodeItemClientWrite(token, jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error(e.toString());
        }
        return ajaxResult;
    }

    @GetMapping(value = "/getEquipmentsStatus", produces = "application/json;charset=UTF-8")
    public AjaxResult getEquipmentsStatus(HttpServletRequest request) throws Exception {

        List<TreatmentEquipment> list = treatmentEquipmentService.getTreatmentEquipment(null);
        System.out.println(list);
        return AjaxResult.success(list);
    }
}