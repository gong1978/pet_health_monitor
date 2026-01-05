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

    @Override
    public BehaviorAnalysisPageResponse pageQuery(BehaviorAnalysisQueryRequest queryRequest) {
        if (queryRequest.getPage() == null || queryRequest.getPage() < 1) queryRequest.setPage(1);
        if (queryRequest.getSize() == null || queryRequest.getSize() < 1) queryRequest.setSize(10);

        QueryWrapper<BehaviorAnalysis> queryWrapper = new QueryWrapper<>();
        if (queryRequest.getPetId() != null) queryWrapper.eq("pet_id", queryRequest.getPetId());
        if (StringUtils.hasText(queryRequest.getBehaviorType())) queryWrapper.eq("behavior_type", queryRequest.getBehaviorType());

        queryWrapper.orderByDesc("start_time");

        Page<BehaviorAnalysis> page = new Page<>(queryRequest.getPage(), queryRequest.getSize());
        page = this.page(page, queryWrapper);

        List<BehaviorAnalysis> records = page.getRecords();
        Map<Integer, String> petNameMap = getPetNameMap(records);

        List<BehaviorAnalysisPageResponse.BehaviorAnalysisResponse> responseList = records.stream()
                .map(behavior -> {
                    Long duration = calculateDuration(behavior.getStartTime(), behavior.getEndTime());
                    return BehaviorAnalysisPageResponse.BehaviorAnalysisResponse.builder()
                            .behaviorId(behavior.getBehaviorId())
                            .petId(behavior.getPetId())
                            .petName(petNameMap.getOrDefault(behavior.getPetId(), "未知"))
                            .behaviorType(behavior.getBehaviorType())
                            .imageUrl(behavior.getImageUrl())
                            .startTime(behavior.getStartTime() != null ? behavior.getStartTime().format(formatter) : "")
                            .endTime(behavior.getEndTime() != null ? behavior.getEndTime().format(formatter) : "")
                            .duration(duration)
                            .confidence(behavior.getConfidence())
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

    /**
     * 创建行为记录 (保留之前的 5分钟截断 + 仿AI随机逻辑)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createBehaviorAnalysis(BehaviorAnalysisCreateRequest createRequest) {
        if (createRequest.getPetId() == null) throw new RuntimeException("宠物ID不能为空");

        Pet pet = petService.getPetById(createRequest.getPetId());
        if (pet == null) throw new RuntimeException("关联的宠物不存在");

        LocalDateTime newStartTime = parseTime(createRequest.getStartTime());
        if (newStartTime == null) newStartTime = LocalDateTime.now();

        // 1. 查找上一条未结束的行为
        QueryWrapper<BehaviorAnalysis> lastRecordQuery = new QueryWrapper<>();
        lastRecordQuery.eq("pet_id", createRequest.getPetId())
                .isNull("end_time")
                .ne(createRequest.getEndTime() != null, "behavior_id", -1)
                .orderByDesc("start_time")
                .last("LIMIT 1");

        BehaviorAnalysis lastRecord = this.getOne(lastRecordQuery);

        if (lastRecord != null && lastRecord.getStartTime().isBefore(newStartTime)) {
            // [5分钟限制逻辑]
            LocalDateTime maxAllowedEndTime = lastRecord.getStartTime().plusMinutes(5);
            LocalDateTime finalEndTime = newStartTime;
            if (newStartTime.isAfter(maxAllowedEndTime)) {
                finalEndTime = maxAllowedEndTime;
            }
            lastRecord.setEndTime(finalEndTime);
            this.updateById(lastRecord);
            log.info("自动结束上一条行为: behaviorId={}, endTime={}", lastRecord.getBehaviorId(), finalEndTime);
        }

        // 2. [仿AI置信度]
        Float finalConfidence = createRequest.getConfidence();
        if (finalConfidence == null) {
            float min = 0.85f, max = 0.99f;
            finalConfidence = min + (float)(Math.random() * (max - min));
            finalConfidence = (float)(Math.round(finalConfidence * 100)) / 100;
        }

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

    /**
     * [核心修复] 更新行为记录 - 增加无变更检测，防止 SQL 报错
     */
    @Override
    public void updateBehaviorAnalysis(BehaviorAnalysisUpdateRequest updateRequest) {
        if (updateRequest.getBehaviorId() == null) throw new RuntimeException("ID不能为空");

        // 1. 先查旧值，确保存在
        BehaviorAnalysis existing = this.getById(updateRequest.getBehaviorId());
        if (existing == null) throw new RuntimeException("记录不存在");

        BehaviorAnalysis.BehaviorAnalysisBuilder builder = BehaviorAnalysis.builder();
        builder.behaviorId(updateRequest.getBehaviorId());

        // [新增] 变更标记
        boolean isChanged = false;

        // 2. 逐个比对：只有值确实变了，才设置到 builder 里
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

        // [重点] 开始时间：解析后对比
        if (StringUtils.hasText(updateRequest.getStartTime())) {
            LocalDateTime newStart = parseTime(updateRequest.getStartTime());
            if (newStart != null && !newStart.equals(existing.getStartTime())) {
                builder.startTime(newStart);
                isChanged = true;
            }
        }

        // [重点] 结束时间：解析后对比
        if (StringUtils.hasText(updateRequest.getEndTime())) {
            LocalDateTime newEnd = parseTime(updateRequest.getEndTime());
            // 如果原来的结束时间是 null，而现在传了新值，也算变更
            // 如果原来有值，新值不同，也算变更
            if (newEnd != null && !newEnd.equals(existing.getEndTime())) {
                builder.endTime(newEnd);
                isChanged = true;
            }
        }

        // 3. [关键逻辑] 如果没有变更，直接返回成功，不执行 SQL
        if (!isChanged) {
            log.info("数据未变更，跳过数据库更新: behaviorId={}", updateRequest.getBehaviorId());
            return;
        }

        // 4. 执行更新
        if (!this.updateById(builder.build())) {
            throw new RuntimeException("更新失败");
        }
        log.info("更新成功: behaviorId={}", updateRequest.getBehaviorId());
    }

    private LocalDateTime parseTime(String timeStr) {
        if (!StringUtils.hasText(timeStr)) return null;
        try {
            return LocalDateTime.parse(timeStr, formatter);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private Long calculateDuration(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) return null;
        return Duration.between(startTime, endTime).toMinutes();
    }

    private Map<Integer, String> getPetNameMap(List<BehaviorAnalysis> records) {
        List<Integer> petIds = records.stream().map(BehaviorAnalysis::getPetId).distinct().collect(Collectors.toList());
        if (petIds.isEmpty()) return Map.of();
        return petService.listByIds(petIds).stream()
                .collect(Collectors.toMap(Pet::getPetId, p -> p.getName() != null ? p.getName() : "未知"));
    }

    @Override
    public BehaviorAnalysis getBehaviorAnalysisById(Long behaviorId) { return this.getById(behaviorId); }
    @Override
    public void deleteBehaviorAnalysis(Long behaviorId) { this.removeById(behaviorId); }
    @Override
    public void batchDeleteBehaviorAnalysis(List<Long> behaviorIds) { this.removeByIds(behaviorIds); }
}