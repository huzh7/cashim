package com.taiji.opcuabackend.controller;


import com.taiji.opcuabackend.entity.AjaxResult;
import com.taiji.opcuabackend.entity.DailyValue;
import com.taiji.opcuabackend.service.DailyValueService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/statistic")
public class StatisticController {

    @Autowired
    private DailyValueService dailyValueService;


    @RequestMapping(value = "/health", produces = "application/json;charset=UTF-8")
    public String updateDailyValue() throws Exception {
        return String.valueOf("ok");
    }


    /**
     * 获取日值统计数据
     * @return
     */
    @ApiOperation(value = "获取日值统计数据")
    @GetMapping(value = "/getDailyValue", produces = "application/json;charset=UTF-8")
    public AjaxResult getDailyValue() throws Exception {
        List<DailyValue> dailyValues = dailyValueService.findValuesWithinLastSevenDays();
        return AjaxResult.success("dailyValues",dailyValues);
    }

}
