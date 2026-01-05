package com.petcare.controller;

import com.petcare.common.Result;
import com.petcare.dto.HealthReportCreateRequest;
import com.petcare.dto.HealthReportPageResponse;
import com.petcare.dto.HealthReportQueryRequest;
import com.petcare.dto.HealthReportUpdateRequest;
import com.petcare.entity.HealthReport;
import com.petcare.service.HealthReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 健康报告控制器
 */
@Slf4j
@RestController
@RequestMapping("/health-reports")
public class HealthReportController {

    @Autowired
    private HealthReportService healthReportService;

    /**
     * 分页查询健康报告
     */
    @GetMapping
    public Result<HealthReportPageResponse> pageQuery(
            @RequestParam(required = false) Integer petId,
            @RequestParam(required = false) Integer minHealthScore,
            @RequestParam(required = false) Integer maxHealthScore,
            @RequestParam(required = false) Integer reviewedBy,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        log.info("分页查询健康报告: petId={}, page={}, size={}", petId, page, size);
        
        try {
            HealthReportQueryRequest queryRequest = HealthReportQueryRequest.builder()
                    .petId(petId)
                    .minHealthScore(minHealthScore)
                    .maxHealthScore(maxHealthScore)
                    .reviewedBy(reviewedBy)
                    .startDate(startDate)
                    .endDate(endDate)
                    .page(page)
                    .size(size)
                    .build();
            
            HealthReportPageResponse response = healthReportService.pageQuery(queryRequest);
            return Result.success(response);
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 根据ID查询健康报告详情
     */
    @GetMapping("/{reportId}")
    public Result<HealthReport> getHealthReportById(@PathVariable Integer reportId) {
        log.info("查询健康报告详情: {}", reportId);
        try {
            HealthReport healthReport = healthReportService.getHealthReportById(reportId);
            return Result.success(healthReport);
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 创建健康报告
     */
    @PostMapping
    public Result<String> createHealthReport(@RequestBody HealthReportCreateRequest createRequest, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未授权");
        }
        
        log.info("创建健康报告: petId={}", createRequest.getPetId());
        try {
            healthReportService.createHealthReport(createRequest);
            return Result.success("创建健康报告成功");
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 更新健康报告
     */
    @PutMapping("/{reportId}")
    public Result<String> updateHealthReport(@PathVariable Integer reportId, 
                                           @RequestBody HealthReportUpdateRequest updateRequest,
                                           HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未授权");
        }
        
        // 设置报告ID
        updateRequest.setReportId(reportId);
        
        log.info("更新健康报告: {}", reportId);
        try {
            healthReportService.updateHealthReport(updateRequest);
            return Result.success("更新健康报告成功");
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 删除健康报告
     */
    @DeleteMapping("/{reportId}")
    public Result<String> deleteHealthReport(@PathVariable Integer reportId, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未授权");
        }
        
        log.info("删除健康报告: {}", reportId);
        try {
            healthReportService.deleteHealthReport(reportId);
            return Result.success("删除健康报告成功");
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 批量删除健康报告
     */
    @DeleteMapping("/batch")
    public Result<String> batchDeleteHealthReports(@RequestBody List<Integer> reportIds, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未授权");
        }
        
        log.info("批量删除健康报告: {}", reportIds);
        try {
            healthReportService.batchDeleteHealthReports(reportIds);
            return Result.success("批量删除健康报告成功");
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }
}
