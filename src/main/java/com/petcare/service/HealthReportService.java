package com.petcare.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.petcare.dto.HealthReportCreateRequest;
import com.petcare.dto.HealthReportPageResponse;
import com.petcare.dto.HealthReportQueryRequest;
import com.petcare.dto.HealthReportUpdateRequest;
import com.petcare.entity.HealthReport;

import java.util.List;

/**
 * 健康报告服务接口
 */
public interface HealthReportService extends IService<HealthReport> {
    /**
     * 分页查询健康报告
     */
    HealthReportPageResponse pageQuery(HealthReportQueryRequest queryRequest);

    /**
     * 根据ID查询健康报告
     */
    HealthReport getHealthReportById(Integer reportId);

    /**
     * 创建健康报告
     */
    void createHealthReport(HealthReportCreateRequest createRequest);

    /**
     * 更新健康报告
     */
    void updateHealthReport(HealthReportUpdateRequest updateRequest);

    /**
     * 删除健康报告
     */
    void deleteHealthReport(Integer reportId);

    /**
     * 批量删除健康报告
     */
    void batchDeleteHealthReports(List<Integer> reportIds);
}
