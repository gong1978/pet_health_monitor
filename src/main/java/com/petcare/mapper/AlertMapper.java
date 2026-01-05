package com.petcare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcare.entity.Alert;
import org.apache.ibatis.annotations.Mapper;

/**
 * 异常预警Mapper接口
 */
@Mapper
public interface AlertMapper extends BaseMapper<Alert> {
}
