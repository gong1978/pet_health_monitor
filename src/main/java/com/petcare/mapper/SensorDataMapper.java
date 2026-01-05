package com.petcare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcare.entity.SensorData;
import org.apache.ibatis.annotations.Mapper;

/**
 * 传感器数据Mapper接口
 */
@Mapper
public interface SensorDataMapper extends BaseMapper<SensorData> {
}
