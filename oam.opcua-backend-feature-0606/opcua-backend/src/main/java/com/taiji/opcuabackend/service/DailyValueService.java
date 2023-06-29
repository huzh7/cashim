package com.taiji.opcuabackend.service;


import com.taiji.opcuabackend.entity.DailyValue;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 统计每日值
 */
public interface DailyValueService {

    public void updateDailyValue(String nodeName, String nodeValue) throws Exception;

    public void insertDailyKey() throws Exception;

    public List<DailyValue> findValuesWithinLastSevenDays() throws Exception;

}
