package com.petcare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcare.entity.VetConsultation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 兽医咨询Mapper接口
 */
@Mapper
public interface VetConsultationMapper extends BaseMapper<VetConsultation> {
}
