package com.taiji.opcuabackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taiji.opcuabackend.entity.ErrorNodeEnum;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;


@Mapper
@Component
public interface ErrorNodeEnumMapper extends BaseMapper<ErrorNodeEnum> {


}
