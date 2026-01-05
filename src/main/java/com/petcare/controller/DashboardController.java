package com.petcare.controller;

import com.petcare.common.Result;
import com.petcare.dto.DashboardStatsResponse;
import com.petcare.service.DashboardStatsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 首页统计控制器
 */
@Slf4j
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardStatsService dashboardStatsService;

    /**
     * 获取首页统计数据
     */
    @GetMapping("/stats")
    public Result<DashboardStatsResponse> getDashboardStats(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未授权");
        }

        try {
            DashboardStatsResponse stats = dashboardStatsService.getUserStats(userId);
            log.info("获取首页统计数据成功: userId={}, petCount={}, alertCount={}, healthReportCount={}, behaviorAnalysisCount={}", 
                    userId, stats.getPetCount(), stats.getAlertCount(), 
                    stats.getHealthReportCount(), stats.getBehaviorAnalysisCount());
            return Result.success(stats);
        } catch (RuntimeException e) {
            log.error("获取首页统计数据失败: userId={}, error={}", userId, e.getMessage());
            return Result.fail(400, e.getMessage());
        }
    }
}
