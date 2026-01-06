package com.petcare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petcare.dto.BehaviorAnalysisCreateRequest;
import com.petcare.dto.BehaviorAnalysisPageResponse;
import com.petcare.dto.BehaviorAnalysisQueryRequest;
import com.petcare.dto.BehaviorAnalysisUpdateRequest;
import com.petcare.entity.BehaviorAnalysis;
import com.petcare.entity.Pet;
import com.petcare.mapper.BehaviorAnalysisMapper;
import com.petcare.service.BehaviorAnalysisService;
import com.petcare.service.PetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 行为识别服务实现
 */
@Slf4j
@Service
public class BehaviorAnalysisServiceImpl extends ServiceImpl<BehaviorAnalysisMapper, BehaviorAnalysis> implements BehaviorAnalysisService {

    @Autowired
    private PetService petService;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // [配置] 最大持续时间为 30 分钟
    private static final long MAX_DURATION_MINUTES = 30;

    @Override
    public BehaviorAnalysisPageResponse pageQuery(BehaviorAnalysisQueryRequest queryRequest) {
        if (queryRequest.getPage() == null || queryRequest.getPage() < 1) queryRequest.setPage(1);
        if (queryRequest.getSize() == null || queryRequest.getSize() < 1) queryRequest.setSize(10);

        QueryWrapper<BehaviorAnalysis> queryWrapper = new QueryWrapper<>();
        if (queryRequest.getPetId() != null) queryWrapper.eq("pet_id", queryRequest.getPetId());
        if (StringUtils.hasText(queryRequest.getBehaviorType())) queryWrapper.eq("behavior_type", queryRequest.getBehaviorType());

        // 其它查询条件
        if (queryRequest.getMinConfidence() != null) queryWrapper.ge("confidence", queryRequest.getMinConfidence());
        if (queryRequest.getMaxConfidence() != null) queryWrapper.le("confidence", queryRequest.getMaxConfidence());
        if (StringUtils.hasText(queryRequest.getStartTimeFrom())) queryWrapper.ge("start_time", parseTime(queryRequest.getStartTimeFrom()));
        if (StringUtils.hasText(queryRequest.getStartTimeTo())) queryWrapper.le("start_time", parseTime(queryRequest.getStartTimeTo()));

        queryWrapper.orderByDesc("start_time");

        Page<BehaviorAnalysis> page = new Page<>(queryRequest.getPage(), queryRequest.getSize());
        this.page(page, queryWrapper);

        List<BehaviorAnalysis> records = page.getRecords();
        Map<Integer, String> petNameMap = getPetNameMap(records);

        List<BehaviorAnalysisPageResponse.BehaviorAnalysisResponse> responseList = records.stream()
                .map(item -> {
                    LocalDateTime start = item.getStartTime();
                    LocalDateTime end = item.getEndTime();

                    // 动态计算显示时长
                    Long displayDuration = null;

                    // [核心逻辑] 如果当前是"进行中"(end==null)，且距离开始时间已超过30分钟
                    if (end == null && start != null) {
                        LocalDateTime autoEndTime = start.plusMinutes(MAX_DURATION_MINUTES);
                        if (LocalDateTime.now().isAfter(autoEndTime)) {
                            end = autoEndTime;
                            displayDuration = MAX_DURATION_MINUTES;
                        }
                    } else if (start != null && end != null) {
                        displayDuration = Duration.between(start, end).toMinutes();
                    }

                    return BehaviorAnalysisPageResponse.BehaviorAnalysisResponse.builder()
                            .behaviorId(item.getBehaviorId())
                            .petId(item.getPetId())
                            .petName(petNameMap.getOrDefault(item.getPetId(), "未知"))
                            .behaviorType(item.getBehaviorType())
                            .imageUrl(item.getImageUrl())
                            .startTime(start != null ? start.format(formatter) : "")
                            .endTime(end != null ? end.format(formatter) : "进行中")
                            .duration(displayDuration)
                            .confidence(item.getConfidence())
                            .build();
                })
                .collect(Collectors.toList());

        return BehaviorAnalysisPageResponse.builder()
                .records(responseList)
                .total(page.getTotal())
                .current(page.getCurrent())
                .size(page.getSize())
                .pages(page.getPages())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createBehaviorAnalysis(BehaviorAnalysisCreateRequest createRequest) {
        if (createRequest.getPetId() == null) throw new RuntimeException("宠物ID不能为空");

        Pet pet = petService.getPetById(createRequest.getPetId());
        if (pet == null) throw new RuntimeException("关联的宠物不存在");

        LocalDateTime newStartTime = parseTime(createRequest.getStartTime());
        if (newStartTime == null) newStartTime = LocalDateTime.now();

        // 1. 查找该宠物上一条"未结束"的行为
        QueryWrapper<BehaviorAnalysis> lastRecordQuery = new QueryWrapper<>();
        lastRecordQuery.eq("pet_id", createRequest.getPetId())
                .isNull("end_time")
                .orderByDesc("start_time")
                .last("LIMIT 1");

        BehaviorAnalysis lastRecord = this.getOne(lastRecordQuery);

        // [核心修改] 截断上一条行为：增加 30 分钟封顶逻辑
        if (lastRecord != null) {
            LocalDateTime lastStart = lastRecord.getStartTime();
            LocalDateTime actualEnd = newStartTime;
            LocalDateTime maxAllowedEnd = lastStart.plusMinutes(MAX_DURATION_MINUTES);

            if (actualEnd.isAfter(maxAllowedEnd)) {
                actualEnd = maxAllowedEnd;
            }

            lastRecord.setEndTime(actualEnd);
            this.updateById(lastRecord);
            log.info("自动结束上一条行为(30分钟封顶): behaviorId={}", lastRecord.getBehaviorId());
        }

        // 2. 仿AI置信度生成
        Float finalConfidence = createRequest.getConfidence();
        if (finalConfidence == null) {
            float min = 0.85f, max = 0.99f;
            finalConfidence = min + (float)(Math.random() * (max - min));
            finalConfidence = (float)(Math.round(finalConfidence * 100)) / 100;
        }

        // 3. 创建新行为
        BehaviorAnalysis behaviorAnalysis = BehaviorAnalysis.builder()
                .petId(createRequest.getPetId())
                .behaviorType(createRequest.getBehaviorType())
                .imageUrl(createRequest.getImageUrl())
                .startTime(newStartTime)
                .endTime(parseTime(createRequest.getEndTime()))
                .confidence(finalConfidence)
                .build();

        if (!this.save(behaviorAnalysis)) throw new RuntimeException("创建失败");
        log.info("创建成功: behaviorId={}", behaviorAnalysis.getBehaviorId());
    }

    @Override
    public void updateBehaviorAnalysis(BehaviorAnalysisUpdateRequest updateRequest) {
        if (updateRequest.getBehaviorId() == null) throw new RuntimeException("ID不能为空");

        BehaviorAnalysis existing = this.getById(updateRequest.getBehaviorId());
        if (existing == null) throw new RuntimeException("记录不存在");

        BehaviorAnalysis.BehaviorAnalysisBuilder builder = BehaviorAnalysis.builder();
        builder.behaviorId(updateRequest.getBehaviorId());

        boolean isChanged = false;

        // 字段比对逻辑
        if (updateRequest.getPetId() != null && !updateRequest.getPetId().equals(existing.getPetId())) {
            builder.petId(updateRequest.getPetId());
            isChanged = true;
        }
        if (StringUtils.hasText(updateRequest.getBehaviorType()) && !updateRequest.getBehaviorType().equals(existing.getBehaviorType())) {
            builder.behaviorType(updateRequest.getBehaviorType());
            isChanged = true;
        }
        if (updateRequest.getConfidence() != null && !updateRequest.getConfidence().equals(existing.getConfidence())) {
            builder.confidence(updateRequest.getConfidence());
            isChanged = true;
        }
        if (StringUtils.hasText(updateRequest.getImageUrl()) && !updateRequest.getImageUrl().equals(existing.getImageUrl())) {
            builder.imageUrl(updateRequest.getImageUrl());
            isChanged = true;
        }

        // 时间比对
        if (StringUtils.hasText(updateRequest.getStartTime())) {
            LocalDateTime newStart = parseTime(updateRequest.getStartTime());
            if (newStart != null && !newStart.equals(existing.getStartTime())) {
                builder.startTime(newStart);
                isChanged = true;
            }
        }
        if (StringUtils.hasText(updateRequest.getEndTime())) {
            LocalDateTime newEnd = parseTime(updateRequest.getEndTime());
            if (newEnd != null && !newEnd.equals(existing.getEndTime())) {
                builder.endTime(newEnd);
                isChanged = true;
            }
        }

        if (isChanged) {
            if (!this.updateById(builder.build())) {
                throw new RuntimeException("更新失败");
            }
            log.info("更新成功: behaviorId={}", updateRequest.getBehaviorId());
        } else {
            log.info("数据未变更，跳过更新: behaviorId={}", updateRequest.getBehaviorId());
        }
    }

    private LocalDateTime parseTime(String timeStr) {
        if (!StringUtils.hasText(timeStr)) return null;
        try {
            return LocalDateTime.parse(timeStr, formatter);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private Map<Integer, String> getPetNameMap(List<BehaviorAnalysis> records) {
        List<Integer> petIds = records.stream().map(BehaviorAnalysis::getPetId).distinct().collect(Collectors.toList());
        if (petIds.isEmpty()) return Map.of();
        return petService.listByIds(petIds).stream()
                .collect(Collectors.toMap(Pet::getPetId, p -> p.getName() != null ? p.getName() : "未知"));
    }

    // 接口要求的 Long 类型方法 (必须有 @Override)
    @Override
    public BehaviorAnalysis getBehaviorAnalysisById(Long behaviorId) { return this.getById(behaviorId); }
    @Override
    public void deleteBehaviorAnalysis(Long behaviorId) { this.removeById(behaviorId); }
    @Override
    public void batchDeleteBehaviorAnalysis(List<Long> behaviorIds) { this.removeByIds(behaviorIds); }

    // 兼容 Integer 类型的辅助方法 (可选，但不能有 @Override，也不能与 Long 版本发生擦除冲突)
    public BehaviorAnalysis getBehaviorAnalysisById(Integer id) {
        if (id == null) return null;
        return this.getById(Long.valueOf(id));
    }
    public void deleteBehaviorAnalysis(Integer id) {
        if (id != null) this.removeById(Long.valueOf(id));
    }

    // [已删除] batchDeleteBehaviorAnalysis(List<Integer> ids)
    // 这个方法必须删除，因为 List<Integer> 和 List<Long> 在编译后是一样的，会导致冲突。
}