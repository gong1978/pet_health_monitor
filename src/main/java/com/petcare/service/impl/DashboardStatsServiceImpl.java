package com.petcare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.petcare.dto.DashboardStatsResponse;
import com.petcare.entity.Alert;
import com.petcare.entity.BehaviorAnalysis;
import com.petcare.entity.HealthReport;
import com.petcare.entity.User;
import com.petcare.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        if (userId == null) throw new RuntimeException("用户ID不能为空");
        User user = userService.getUserById(userId);
        if (user == null) throw new RuntimeException("用户不存在");

        boolean isAdminOrVet = user.getRole() != null && (user.getRole() == 1 || user.getRole() == 2);

        long petCount;
        long alertCount;
        long healthReportCount;
        long behaviorAnalysisCount;

        // [新增] 定义趋势数据容器
        List<String> trendDates = new ArrayList<>();
        List<Long> trendCritical = new ArrayList<>();
        List<Long> trendWarning = new ArrayList<>();

        if (isAdminOrVet) {
            petCount = petService.count();
            alertCount = alertService.count();
            healthReportCount = healthReportService.count();
            behaviorAnalysisCount = behaviorAnalysisService.count();

            // [新增] 管理员/兽医：计算全平台最近7天预警趋势
            calculateAlertTrend(null, trendDates, trendCritical, trendWarning);
        } else {
            List<Integer> userPetIds = userPetService.getPetIdsByUserId(userId);
            petCount = userPetIds.size();
            alertCount = getUserPetAlertCount(userPetIds);
            healthReportCount = getUserPetHealthReportCount(userPetIds);
            behaviorAnalysisCount = getUserPetBehaviorAnalysisCount(userPetIds);

            // [新增] 普通用户：计算自己宠物最近7天预警趋势（虽然普通用户首页可能不显示图表，但为了接口统一还是返回）
            if (!userPetIds.isEmpty()) {
                calculateAlertTrend(userPetIds, trendDates, trendCritical, trendWarning);
            }
        }

        return DashboardStatsResponse.builder()
                .petCount(petCount)
                .alertCount(alertCount)
                .healthReportCount(healthReportCount)
                .behaviorAnalysisCount(behaviorAnalysisCount)
                .userRole(user.getRole())
                .userName(user.getFullName() != null ? user.getFullName() : user.getUsername())
                // [新增] 注入趋势数据
                .trendDates(trendDates)
                .trendCritical(trendCritical)
                .trendWarning(trendWarning)
                .build();
    }

    /**
     * [新增] 计算最近7天的预警趋势
     */
    private void calculateAlertTrend(List<Integer> petIds, List<String> dates, List<Long> criticals, List<Long> warnings) {
        // 1. 获取7天前的时间点
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(6); // 包括今天共7天
        LocalDateTime startDateTime = sevenDaysAgo.atStartOfDay();

        // 2. 查询时间段内的所有 Alert
        QueryWrapper<Alert> wrapper = new QueryWrapper<>();
        wrapper.ge("created_at", startDateTime);
        if (petIds != null && !petIds.isEmpty()) {
            wrapper.in("pet_id", petIds);
        }
        List<Alert> recentAlerts = alertService.list(wrapper);

        // 3. 按日期 + 等级 分组统计
        // Map<日期字符串, Map<等级, 数量>>
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");

        // 预处理数据：将 Alert 列表转为 Map 便于查找
        Map<String, Map<String, Long>> statsMap = recentAlerts.stream()
                .collect(Collectors.groupingBy(
                        alert -> alert.getCreatedAt().format(fmt),
                        Collectors.groupingBy(
                                alert -> alert.getLevel() == null ? "warning" : alert.getLevel(),
                                Collectors.counting()
                        )
                ));

        // 4. 填充最近7天的数据（确保每天都有数据，即使是0）
        for (int i = 0; i < 7; i++) {
            LocalDate date = sevenDaysAgo.plusDays(i);
            String dateStr = date.format(fmt);

            dates.add(dateStr); // X轴日期

            Map<String, Long> dailyStats = statsMap.getOrDefault(dateStr, Map.of());
            criticals.add(dailyStats.getOrDefault("critical", 0L));
            warnings.add(dailyStats.getOrDefault("warning", 0L));
        }
    }

    // ... (保留原有的 getUserPetAlertCount 等辅助方法) ...
    private long getUserPetAlertCount(List<Integer> petIds) {
        if (petIds == null || petIds.isEmpty()) return 0L;
        return alertService.count(new QueryWrapper<Alert>().in("pet_id", petIds));
    }
    private long getUserPetHealthReportCount(List<Integer> petIds) {
        if (petIds == null || petIds.isEmpty()) return 0L;
        return healthReportService.count(new QueryWrapper<HealthReport>().in("pet_id", petIds));
    }
    private long getUserPetBehaviorAnalysisCount(List<Integer> petIds) {
        if (petIds == null || petIds.isEmpty()) return 0L;
        return behaviorAnalysisService.count(new QueryWrapper<BehaviorAnalysis>().in("pet_id", petIds));
    }
}