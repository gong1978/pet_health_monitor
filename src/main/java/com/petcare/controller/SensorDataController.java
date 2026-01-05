package com.petcare.controller;

import com.petcare.common.Result;
import com.petcare.dto.SensorDataCreateRequest;
import com.petcare.dto.SensorDataPageResponse;
import com.petcare.dto.SensorDataQueryRequest;
import com.petcare.dto.SensorDataUpdateRequest;
import com.petcare.entity.SensorData;
import com.petcare.service.SensorDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 传感器数据控制器
 */
@Slf4j
@RestController
@RequestMapping("/sensor-data")
public class SensorDataController {

    @Autowired
    private SensorDataService sensorDataService;

    /**
     * 分页查询传感器数据
     */
    @GetMapping
    public Result<SensorDataPageResponse> pageQuery(
            @RequestParam(required = false) Integer petId,
            @RequestParam(required = false) Integer minHeartRate,
            @RequestParam(required = false) Integer maxHeartRate,
            @RequestParam(required = false) Float minTemperature,
            @RequestParam(required = false) Float maxTemperature,
            @RequestParam(required = false) Integer minActivity,
            @RequestParam(required = false) Integer maxActivity,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        log.info("分页查询传感器数据: petId={}, page={}, size={}", petId, page, size);
        
        try {
            SensorDataQueryRequest queryRequest = SensorDataQueryRequest.builder()
                    .petId(petId)
                    .minHeartRate(minHeartRate)
                    .maxHeartRate(maxHeartRate)
                    .minTemperature(minTemperature)
                    .maxTemperature(maxTemperature)
                    .minActivity(minActivity)
                    .maxActivity(maxActivity)
                    .startTime(startTime)
                    .endTime(endTime)
                    .page(page)
                    .size(size)
                    .build();
            
            SensorDataPageResponse response = sensorDataService.pageQuery(queryRequest);
            return Result.success(response);
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 根据ID查询传感器数据详情
     */
    @GetMapping("/{dataId}")
    public Result<SensorData> getSensorDataById(@PathVariable Long dataId) {
        log.info("查询传感器数据详情: {}", dataId);
        try {
            SensorData sensorData = sensorDataService.getSensorDataById(dataId);
            return Result.success(sensorData);
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 创建传感器数据
     */
    @PostMapping
    public Result<String> createSensorData(@RequestBody SensorDataCreateRequest createRequest, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未授权");
        }
        
        log.info("创建传感器数据: petId={}", createRequest.getPetId());
        try {
            sensorDataService.createSensorData(createRequest);
            return Result.success("创建传感器数据成功");
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 更新传感器数据
     */
    @PutMapping("/{dataId}")
    public Result<String> updateSensorData(@PathVariable Long dataId, 
                                         @RequestBody SensorDataUpdateRequest updateRequest,
                                         HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未授权");
        }
        
        // 设置数据ID
        updateRequest.setDataId(dataId);
        
        log.info("更新传感器数据: {}", dataId);
        try {
            sensorDataService.updateSensorData(updateRequest);
            return Result.success("更新传感器数据成功");
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 删除传感器数据
     */
    @DeleteMapping("/{dataId}")
    public Result<String> deleteSensorData(@PathVariable Long dataId, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未授权");
        }
        
        log.info("删除传感器数据: {}", dataId);
        try {
            sensorDataService.deleteSensorData(dataId);
            return Result.success("删除传感器数据成功");
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 批量删除传感器数据
     */
    @DeleteMapping("/batch")
    public Result<String> batchDeleteSensorData(@RequestBody List<Long> dataIds, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未授权");
        }
        
        log.info("批量删除传感器数据: {}", dataIds);
        try {
            sensorDataService.batchDeleteSensorData(dataIds);
            return Result.success("批量删除传感器数据成功");
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }
}
