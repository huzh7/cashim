package com.taiji.opcuabackend.task;


import com.taiji.opcuabackend.service.CameraInfoService;
import com.taiji.opcuabackend.service.impl.DailyValueServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;

@Configuration
@EnableScheduling
@Slf4j
public class TimeTask {

    @Resource
    DailyValueServiceImpl dailyValueService;

    @Resource
    CameraInfoService cameraInfoService;

    // 每天晚上 0:0:0 执行一次
    @Scheduled(cron = "0 0 0 * * ?")
    public void insertDailyValue() throws Exception {
        dailyValueService.insertDailyKey();
    }

    // 每5分钟执行一次，用于ping camera ip是否可用
    @Scheduled(cron = "0 0/30 * * * ?")
    public void pingCameraIp() throws Exception {
        cameraInfoService.updateCameraStatus();
    }

}
