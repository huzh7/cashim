package com.taiji.opcuabackend.service.impl;

import com.taiji.opcuabackend.entity.DailyValue;
import com.taiji.opcuabackend.mapper.DailyValueMapper;
import com.taiji.opcuabackend.service.DailyValueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class DailyValueServiceImpl implements DailyValueService {

    @Autowired
    private DailyValueMapper dailyValueMapper;

    @Override
    public void updateDailyValue(String nodeName, String nodeValue) throws Exception {
        if(nodeName == null || "".equals(nodeName)){
            log.error("需要添加的ErrorNodeType为null或者''");
            throw new IllegalArgumentException("需要添加的ErrorNodeType为null或者''");
        }

        // 获取今天为时间戳为key
        LocalDate today = LocalDate.now();
        Date dateKey = Date.valueOf(today);
        DailyValue dailyValue = dailyValueMapper.selectByPrimaryKey(dateKey);
        String lastNodeName = new String();
        if(dailyValue != null) {
            if(nodeName.contains(".")) {
                String[] errorNodeArray = nodeName.split("\\.");
                lastNodeName = errorNodeArray[errorNodeArray.length - 1];
            } else {
                lastNodeName = nodeName;
            }
            if("Status".equals(lastNodeName)) {
                // 如果是连锁 false 则更新 +1 数据
                dailyValue.setStatusCount(dailyValue.getStatusCount() + 1);
                dailyValueMapper.updateByPrimaryKeySelective(dailyValue);
            }
        }
    }

    @Override
    public void insertDailyKey() throws Exception  {
        log.info("创建当前日期");

        // 获取今天为时间戳为 key
        try {
            LocalDate today = LocalDate.now();
            Date dateKey = Date.valueOf(today);
            DailyValue dailyValue = dailyValueMapper.selectByPrimaryKey(dateKey);
            if(dailyValue == null) {
                DailyValue d = new DailyValue();
                d.setDateKey(dateKey);
                d.setStatusCount(0L);
                dailyValueMapper.insert(d);
            }
        } catch (Exception e) {
            log.error("创建当前日期失败");
            throw new Exception("创建当前日期失败");
        }
    }


    @Override
    public List<DailyValue> findValuesWithinLastSevenDays() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);
        Date sqlStartDate = Date.valueOf(startDate);
        Date sqlEndDate = Date.valueOf(endDate);
        return dailyValueMapper.findValuesWithinDateRange(sqlStartDate, sqlEndDate);
    }
}
