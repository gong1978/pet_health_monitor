package com.petcare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcare.entity.HealthReport;
import org.apache.ibatis.annotations.Mapper;

/**
 * 健康报告Mapper接口
 */
@Mapper
public interface HealthReportMapper extends BaseMapper<HealthReport> {
}
