package com.petcare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.petcare.dto.DashboardStatsResponse;
import com.petcare.entity.Alert;
import com.petcare.entity.BehaviorAnalysis;
import com.petcare.entity.HealthReport;
import com.petcare.entity.Pet;
import com.petcare.entity.User;
import com.petcare.service.AlertService;
import com.petcare.service.BehaviorAnalysisService;
import com.petcare.service.DashboardStatsService;
import com.petcare.service.HealthReportService;
import com.petcare.service.PetService;
import com.petcare.service.UserPetService;
import com.petcare.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 首页统计数据服务实现
 */
@Slf4j
@Service
public class DashboardStatsServiceImpl implements DashboardStatsService {

    @Autowired
    private UserService userService;

    @Autowired
    private PetService petService;

    @Autowired
    private UserPetService userPetService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private HealthReportService healthReportService;

    @Autowired
    private BehaviorAnalysisService behaviorAnalysisService;

    @Override
    public DashboardStatsResponse getUserStats(Integer userId) {
        if (userId == null) {
            throw new RuntimeException("用户ID不能为空");
        }

        // 获取用户信息
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 根据用户角色获取不同的统计数据
        boolean isAdminOrVet = user.getRole() != null && (user.getRole() == 1 || user.getRole() == 2);
        
        long petCount;
        long alertCount;
        long healthReportCount; 
        long behaviorAnalysisCount;

        if (isAdminOrVet) {
            // 管理员和兽医：显示所有数据统计
            petCount = getAllPetCount();
            alertCount = getAllAlertCount();
            healthReportCount = getAllHealthReportCount();
            behaviorAnalysisCount = getAllBehaviorAnalysisCount();
        } else {
            // 普通用户：显示自己宠物相关的数据统计
            List<Integer> userPetIds = userPetService.getPetIdsByUserId(userId);
            petCount = userPetIds.size();
            alertCount = getUserPetAlertCount(userPetIds);
            healthReportCount = getUserPetHealthReportCount(userPetIds);
            behaviorAnalysisCount = getUserPetBehaviorAnalysisCount(userPetIds);
        }

        // 构建响应
        return DashboardStatsResponse.builder()
                .petCount(petCount)
                .alertCount(alertCount)
                .healthReportCount(healthReportCount)
                .behaviorAnalysisCount(behaviorAnalysisCount)
                .userRole(user.getRole())
                .userName(user.getFullName() != null ? user.getFullName() : user.getUsername())
                .build();
    }

    /**
     * 获取所有宠物数量
     */
    private long getAllPetCount() {
        try {
            return petService.count();
        } catch (Exception e) {
            log.error("获取宠物总数失败", e);
            return 0L;
        }
    }

    /**
     * 获取所有告警数量
     */
    private long getAllAlertCount() {
        try {
            return alertService.count();
        } catch (Exception e) {
            log.error("获取告警总数失败", e);
            return 0L;
        }
    }

    /**
     * 获取所有健康报告数量
     */
    private long getAllHealthReportCount() {
        try {
            return healthReportService.count();
        } catch (Exception e) {
            log.error("获取健康报告总数失败", e);
            return 0L;
        }
    }

    /**
     * 获取所有行为分析数量
     */
    private long getAllBehaviorAnalysisCount() {
        try {
            return behaviorAnalysisService.count();
        } catch (Exception e) {
            log.error("获取行为分析总数失败", e);
            return 0L;
        }
    }

    /**
     * 获取用户宠物相关的告警数量
     */
    private long getUserPetAlertCount(List<Integer> petIds) {
        if (petIds == null || petIds.isEmpty()) {
            return 0L;
        }

        try {
            QueryWrapper<Alert> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("pet_id", petIds);
            return alertService.count(queryWrapper);
        } catch (Exception e) {
            log.error("获取用户宠物告警数量失败", e);
            return 0L;
        }
    }

    /**
     * 获取用户宠物相关的健康报告数量
     */
    private long getUserPetHealthReportCount(List<Integer> petIds) {
        if (petIds == null || petIds.isEmpty()) {
            return 0L;
        }

        try {
            QueryWrapper<HealthReport> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("pet_id", petIds);
            return healthReportService.count(queryWrapper);
        } catch (Exception e) {
            log.error("获取用户宠物健康报告数量失败", e);
            return 0L;
        }
    }

    /**
     * 获取用户宠物相关的行为分析数量
     */
    private long getUserPetBehaviorAnalysisCount(List<Integer> petIds) {
        if (petIds == null || petIds.isEmpty()) {
            return 0L;
        }

        try {
            QueryWrapper<BehaviorAnalysis> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("pet_id", petIds);
            return behaviorAnalysisService.count(queryWrapper);
        } catch (Exception e) {
            log.error("获取用户宠物行为分析数量失败", e);
            return 0L;
        }
    }
}
