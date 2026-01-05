package com.petcare.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.petcare.dto.AlertCreateRequest;
import com.petcare.dto.AlertPageResponse;
import com.petcare.dto.AlertQueryRequest;
import com.petcare.dto.AlertUpdateRequest;
import com.petcare.entity.Alert;

import java.util.List;

/**
 * 异常预警服务接口
 */
public interface AlertService extends IService<Alert> {
    /**
     * 分页查询异常预警
     */
    AlertPageResponse pageQuery(AlertQueryRequest queryRequest);

    /**
     * 根据ID查询异常预警
     */
    Alert getAlertById(Integer alertId);

    /**
     * 创建异常预警
     */
    void createAlert(AlertCreateRequest createRequest);

    /**
     * 更新异常预警
     */
    void updateAlert(AlertUpdateRequest updateRequest);

    /**
     * 删除异常预警
     */
    void deleteAlert(Integer alertId);

    /**
     * 批量删除异常预警
     */
    void batchDeleteAlerts(List<Integer> alertIds);

    /**
     * 处理预警（标记为已处理）
     */
    void resolveAlert(Integer alertId, Integer userId);

    /**
     * 批量处理预警
     */
    void batchResolveAlerts(List<Integer> alertIds, Integer userId);
}
