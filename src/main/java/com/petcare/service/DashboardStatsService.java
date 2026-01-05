package com.petcare.service;

import com.petcare.dto.DashboardStatsResponse;

/**
 * 首页统计数据服务接口
 */
public interface DashboardStatsService {
    
    /**
     * 获取用户的统计数据
     * 管理员和兽医：显示所有数据统计
     * 普通用户：显示自己宠物相关的数据统计
     */
    DashboardStatsResponse getUserStats(Integer userId);
}
