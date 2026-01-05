package com.petcare.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.petcare.dto.VetConsultationCreateRequest;
import com.petcare.dto.VetConsultationPageResponse;
import com.petcare.dto.VetConsultationQueryRequest;
import com.petcare.dto.VetConsultationUpdateRequest;
import com.petcare.entity.VetConsultation;

import java.util.List;

/**
 * 兽医咨询服务接口
 */
public interface VetConsultationService extends IService<VetConsultation> {
    /**
     * 分页查询兽医咨询
     */
    VetConsultationPageResponse pageQuery(VetConsultationQueryRequest queryRequest);

    /**
     * 根据ID查询兽医咨询
     */
    VetConsultation getVetConsultationById(Integer consultId);

    /**
     * 创建兽医咨询
     */
    void createVetConsultation(VetConsultationCreateRequest createRequest);

    /**
     * 更新兽医咨询
     */
    void updateVetConsultation(VetConsultationUpdateRequest updateRequest);

    /**
     * 删除兽医咨询
     */
    void deleteVetConsultation(Integer consultId);

    /**
     * 批量删除兽医咨询
     */
    void batchDeleteVetConsultations(List<Integer> consultIds);

    /**
     * 兽医回答问题
     */
    void answerConsultation(Integer consultId, String answer, Integer vetId);
}
