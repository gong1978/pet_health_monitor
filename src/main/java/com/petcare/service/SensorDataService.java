package com.petcare.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.petcare.dto.SensorDataCreateRequest;
import com.petcare.dto.SensorDataPageResponse;
import com.petcare.dto.SensorDataQueryRequest;
import com.petcare.dto.SensorDataUpdateRequest;
import com.petcare.entity.SensorData;

import java.util.List;

/**
 * 传感器数据服务接口
 */
public interface SensorDataService extends IService<SensorData> {
    /**
     * 分页查询传感器数据
     */
    SensorDataPageResponse pageQuery(SensorDataQueryRequest queryRequest);

    /**
     * 根据ID查询传感器数据
     */
    SensorData getSensorDataById(Long dataId);

    /**
     * 创建传感器数据
     */
    void createSensorData(SensorDataCreateRequest createRequest);

    /**
     * 更新传感器数据
     */
    void updateSensorData(SensorDataUpdateRequest updateRequest);

    /**
     * 删除传感器数据
     */
    void deleteSensorData(Long dataId);

    /**
     * 批量删除传感器数据
     */
    void batchDeleteSensorData(List<Long> dataIds);
}
