package com.taiji.opcuabackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taiji.opcuabackend.entity.ErrorNodeInfo;
import com.taiji.opcuabackend.entity.ErrorNodeInfoStatistics;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;


@Mapper
@Component
public interface ErrorNodeInfoMapper extends BaseMapper<ErrorNodeInfo> {

    /**
     *
     * @return 统计对象信息
     */
    public List<ErrorNodeInfoStatistics> selectStatisticsErrorNodeInfo();

}
