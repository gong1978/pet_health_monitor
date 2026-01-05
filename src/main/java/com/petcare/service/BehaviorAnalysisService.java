package com.petcare.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.petcare.dto.BehaviorAnalysisCreateRequest;
import com.petcare.dto.BehaviorAnalysisPageResponse;
import com.petcare.dto.BehaviorAnalysisQueryRequest;
import com.petcare.dto.BehaviorAnalysisUpdateRequest;
import com.petcare.entity.BehaviorAnalysis;

import java.util.List;

/**
 * 行为识别服务接口
 */
public interface BehaviorAnalysisService extends IService<BehaviorAnalysis> {
    /**
     * 分页查询行为识别
     */
    BehaviorAnalysisPageResponse pageQuery(BehaviorAnalysisQueryRequest queryRequest);

    /**
     * 根据ID查询行为识别
     */
    BehaviorAnalysis getBehaviorAnalysisById(Long behaviorId);

    /**
     * 创建行为识别
     */
    void createBehaviorAnalysis(BehaviorAnalysisCreateRequest createRequest);

    /**
     * 更新行为识别
     */
    void updateBehaviorAnalysis(BehaviorAnalysisUpdateRequest updateRequest);

    /**
     * 删除行为识别
     */
    void deleteBehaviorAnalysis(Long behaviorId);

    /**
     * 批量删除行为识别
     */
    void batchDeleteBehaviorAnalysis(List<Long> behaviorIds);
}
