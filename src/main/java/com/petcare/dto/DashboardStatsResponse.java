package com.petcare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 首页统计数据响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    /**
     * 我的宠物数量
     */
    private Long petCount;

    /**
     * 健康告警数量
     */
    private Long alertCount;

    /**
     * 医疗记录数量
     */
    private Long healthReportCount;

    /**
     * 行为数据数量
     */
    private Long behaviorAnalysisCount;

    /**
     * 用户角色（用于前端判断显示内容）
     */
    private Integer userRole;

    /**
     * 用户昵称（用于显示欢迎信息）
     */
    private String userName;

    // [新增] 趋势图数据
    private List<String> trendDates;      // 日期列表 (X轴)
    private List<Long> trendCritical;     // 严重预警数量 (Y轴1)
    private List<Long> trendWarning;      // 普通预警数量 (Y轴2)
}