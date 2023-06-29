package com.taiji.opcuabackend.mapper;

import com.taiji.opcuabackend.entity.DailyValue;

import java.util.Date;
import java.util.List;

public interface DailyValueMapper {
    int deleteByPrimaryKey(Date dateKey);

    int insert(DailyValue record);

    int insertSelective(DailyValue record);

    DailyValue selectByPrimaryKey(Date dateKey);

    int updateByPrimaryKeySelective(DailyValue record);

    int updateByPrimaryKey(DailyValue record);

    List<DailyValue> findValuesWithinDateRange(java.sql.Date sqlStartDate, java.sql.Date sqlEndDate);
}