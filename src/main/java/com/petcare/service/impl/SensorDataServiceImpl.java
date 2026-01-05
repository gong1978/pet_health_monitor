package com.petcare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petcare.dto.SensorDataCreateRequest;
import com.petcare.dto.SensorDataPageResponse;
import com.petcare.dto.SensorDataQueryRequest;
import com.petcare.dto.SensorDataUpdateRequest;
import com.petcare.entity.Pet;
import com.petcare.entity.SensorData;
import com.petcare.mapper.SensorDataMapper;
import com.petcare.service.PetService;
import com.petcare.service.SensorDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 传感器数据服务实现
 */
@Slf4j
@Service
public class SensorDataServiceImpl extends ServiceImpl<SensorDataMapper, SensorData> implements SensorDataService {

    @Autowired
    private PetService petService;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final Random random = new Random();

    @Override
    public SensorDataPageResponse pageQuery(SensorDataQueryRequest queryRequest) {
        // ... (保持原有的查询代码不变) ...
        if (queryRequest.getPage() == null || queryRequest.getPage() < 1) {
            queryRequest.setPage(1);
        }
        if (queryRequest.getSize() == null || queryRequest.getSize() < 1) {
            queryRequest.setSize(10);
        }

        QueryWrapper<SensorData> queryWrapper = new QueryWrapper<>();

        if (queryRequest.getPetId() != null) queryWrapper.eq("pet_id", queryRequest.getPetId());
        if (queryRequest.getMinHeartRate() != null) queryWrapper.ge("heart_rate", queryRequest.getMinHeartRate());
        if (queryRequest.getMaxHeartRate() != null) queryWrapper.le("heart_rate", queryRequest.getMaxHeartRate());
        if (queryRequest.getMinTemperature() != null) queryWrapper.ge("temperature", queryRequest.getMinTemperature());
        if (queryRequest.getMaxTemperature() != null) queryWrapper.le("temperature", queryRequest.getMaxTemperature());
        if (queryRequest.getMinActivity() != null) queryWrapper.ge("activity", queryRequest.getMinActivity());
        if (queryRequest.getMaxActivity() != null) queryWrapper.le("activity", queryRequest.getMaxActivity());

        if (StringUtils.hasText(queryRequest.getStartTime())) {
            try {
                LocalDateTime startTime = LocalDateTime.parse(queryRequest.getStartTime(), formatter);
                queryWrapper.ge("collected_at", startTime);
            } catch (DateTimeParseException e) {
                throw new RuntimeException("开始时间格式错误");
            }
        }
        if (StringUtils.hasText(queryRequest.getEndTime())) {
            try {
                LocalDateTime endTime = LocalDateTime.parse(queryRequest.getEndTime(), formatter);
                queryWrapper.le("collected_at", endTime);
            } catch (DateTimeParseException e) {
                throw new RuntimeException("结束时间格式错误");
            }
        }

        queryWrapper.orderByDesc("collected_at");

        Page<SensorData> page = new Page<>(queryRequest.getPage(), queryRequest.getSize());
        page = this.page(page, queryWrapper);

        List<SensorData> records = page.getRecords();
        Map<Integer, String> petNameMap = getPetNameMap(records);

        List<SensorDataPageResponse.SensorDataResponse> responseList = records.stream()
                .map(data -> SensorDataPageResponse.SensorDataResponse.builder()
                        .dataId(data.getDataId())
                        .petId(data.getPetId())
                        .petName(petNameMap.get(data.getPetId()))
                        .heartRate(data.getHeartRate())
                        .temperature(data.getTemperature())
                        .activity(data.getActivity())
                        .collectedAt(data.getCollectedAt() != null ? data.getCollectedAt().format(formatter) : "")
                        .build())
                .collect(Collectors.toList());

        return SensorDataPageResponse.builder()
                .records(responseList)
                .total(page.getTotal())
                .current(page.getCurrent())
                .size(page.getSize())
                .pages(page.getPages())
                .build();
    }

    @Override
    public SensorData getSensorDataById(Long dataId) {
        if (dataId == null) throw new RuntimeException("数据ID不能为空");
        SensorData sensorData = this.getById(dataId);
        if (sensorData == null) throw new RuntimeException("传感器数据不存在");
        return sensorData;
    }

    @Override
    public void createSensorData(SensorDataCreateRequest createRequest) {
        if (createRequest.getPetId() == null) throw new RuntimeException("宠物ID不能为空");

        Pet pet = petService.getPetById(createRequest.getPetId());
        if (pet == null) throw new RuntimeException("关联的宠物不存在");

        validateSensorDataRanges(createRequest.getHeartRate(), createRequest.getTemperature(), createRequest.getActivity());

        // 解析时间
        LocalDateTime collectedAt;
        if (StringUtils.hasText(createRequest.getCollectedAt())) {
            try {
                collectedAt = LocalDateTime.parse(createRequest.getCollectedAt(), formatter);
            } catch (DateTimeParseException e) {
                throw new RuntimeException("采集时间格式错误");
            }
        } else {
            collectedAt = LocalDateTime.now();
        }

        // [核心修改]：如果活动量为空，则随机生成 (模拟传感器读取)
        Integer activity = createRequest.getActivity();
        if (activity == null) {
            // 随机生成 0 ~ 3000 之间的活动量
            activity = random.nextInt(3001);
            log.info("模拟生成活动量数据: {}", activity);
        }

        SensorData sensorData = SensorData.builder()
                .petId(createRequest.getPetId())
                .heartRate(createRequest.getHeartRate())
                .temperature(createRequest.getTemperature())
                .activity(activity) // 使用处理后的活动量
                .collectedAt(collectedAt)
                .build();

        boolean success = this.save(sensorData);
        if (!success) throw new RuntimeException("创建传感器数据失败");

        log.info("创建传感器数据成功: petId={}, dataId={}, activity={}", createRequest.getPetId(), sensorData.getDataId(), activity);
    }

    @Override
    public void updateSensorData(SensorDataUpdateRequest updateRequest) {
        if (updateRequest.getDataId() == null) throw new RuntimeException("数据ID不能为空");

        SensorData existData = getSensorDataById(updateRequest.getDataId());
        if (existData == null) throw new RuntimeException("传感器数据不存在");

        if (updateRequest.getPetId() != null && !updateRequest.getPetId().equals(existData.getPetId())) {
            Pet pet = petService.getPetById(updateRequest.getPetId());
            if (pet == null) throw new RuntimeException("关联的宠物不存在");
        }

        validateSensorDataRanges(updateRequest.getHeartRate(), updateRequest.getTemperature(), updateRequest.getActivity());

        LocalDateTime collectedAt = null;
        if (StringUtils.hasText(updateRequest.getCollectedAt())) {
            try {
                collectedAt = LocalDateTime.parse(updateRequest.getCollectedAt(), formatter);
            } catch (DateTimeParseException e) {
                throw new RuntimeException("时间格式错误");
            }
        }

        SensorData sensorData = SensorData.builder()
                .dataId(updateRequest.getDataId())
                .petId(updateRequest.getPetId())
                .heartRate(updateRequest.getHeartRate())
                .temperature(updateRequest.getTemperature())
                .activity(updateRequest.getActivity())
                .collectedAt(collectedAt)
                .build();

        if (!this.updateById(sensorData)) throw new RuntimeException("更新失败");
        log.info("更新成功: dataId={}", updateRequest.getDataId());
    }

    @Override
    public void deleteSensorData(Long dataId) {
        if (dataId == null) throw new RuntimeException("ID不能为空");
        if (this.getById(dataId) == null) throw new RuntimeException("数据不存在");
        if (!this.removeById(dataId)) throw new RuntimeException("删除失败");
        log.info("删除成功: dataId={}", dataId);
    }

    @Override
    public void batchDeleteSensorData(List<Long> dataIds) {
        if (dataIds == null || dataIds.isEmpty()) throw new RuntimeException("列表为空");
        if (!this.removeByIds(dataIds)) throw new RuntimeException("批量删除失败");
        log.info("批量删除成功: {}", dataIds.size());
    }

    private void validateSensorDataRanges(Integer heartRate, Float temperature, Integer activity) {
        if (heartRate != null && (heartRate < 0 || heartRate > 300)) throw new RuntimeException("心率超出范围");
        if (temperature != null && (temperature < 30.0 || temperature > 45.0)) throw new RuntimeException("体温超出范围");
        if (activity != null && activity < 0) throw new RuntimeException("活动量不能为负");
    }

    private Map<Integer, String> getPetNameMap(List<SensorData> records) {
        List<Integer> petIds = records.stream().map(SensorData::getPetId).distinct().collect(Collectors.toList());
        if (petIds.isEmpty()) return Map.of();
        List<Pet> pets = petService.listByIds(petIds);
        return pets.stream().collect(Collectors.toMap(Pet::getPetId, pet -> pet.getName() != null ? pet.getName() : "未知"));
    }
}