package com.petcare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcare.entity.BehaviorAnalysis;
import org.apache.ibatis.annotations.Mapper;

/**
 * 行为识别Mapper接口
 */
@Mapper
public interface BehaviorAnalysisMapper extends BaseMapper<BehaviorAnalysis> {
}
