package com.taiji.opcuabackend.controller;

import com.taiji.opcuabackend.entity.AjaxResult;
import com.taiji.opcuabackend.service.ClientInfoService;
import com.taiji.opcuabackend.service.OpcUaSubscriptionService;
import com.taiji.opcuabackend.service.RegitserService;
import com.taiji.opcuabackend.util.RedisUtil;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;

/**
 * @author sunzb
 */
@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/register")
public class RegisterController {

    @Autowired
    private OpcUaSubscriptionService opcUaSubscriptionService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private RegitserService regitserService;

    @Autowired
    private ClientInfoService clientInfoService;

    /**
     * 注册客户端并生成token返回给客户端
     * @param client
     */
    @ApiOperation(value = "注册客户端并生成token返回给客户端")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "client", value = "客户端", required = true, dataType = "java.lang.String")
    })
    @GetMapping(value = "/getToken", produces = "application/json;charset=UTF-8")
    public AjaxResult getToken(@RequestParam String client, HttpServletRequest request){

        try {
            //获取客户端IP地址
            String ipAddr = getIpAddress(request);

            return regitserService.saveClientInfo(client,ipAddr);
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error(e.toString());
        }

    }

    /**
     *清理非活跃的redis中的node订阅的客户端信息
     */
    @ApiOperation(value = "清理非活跃的redis中的node订阅的客户端信息")
    @DeleteMapping(value = "/cleanRedisClientInfoChannel", produces = "application/json;charset=UTF-8")
    public AjaxResult cleanRedisClientInfoChannel(){

        try {
             clientInfoService.cleanRedisClientInfoChannel();
            return AjaxResult.success("success");
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error(e.toString());
        }

    }

    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknow".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            if (ip.equals("127.0.0.1")) {
                //根据网卡取本机配置的IP
                InetAddress inet = null;
                try {
                    inet = InetAddress.getLocalHost();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ip = inet.getHostAddress();
            }
        }
        // 多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (ip != null && ip.length() > 15) {
            if (ip.indexOf(",") > 0) {
                ip = ip.substring(0, ip.indexOf(","));
            }
        }
        return ip;
    }
}