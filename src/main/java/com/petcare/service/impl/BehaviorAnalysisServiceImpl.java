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
                            .imageUrl(behavior.getImageUrl()) // 核心：回显给前端
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

    @Override
    public void createBehaviorAnalysis(BehaviorAnalysisCreateRequest createRequest) {
        if (createRequest.getPetId() == null) throw new RuntimeException("宠物ID不能为空");

        Pet pet = petService.getPetById(createRequest.getPetId());
        if (pet == null) throw new RuntimeException("关联的宠物不存在");

        BehaviorAnalysis behaviorAnalysis = BehaviorAnalysis.builder()
                .petId(createRequest.getPetId())
                .behaviorType(createRequest.getBehaviorType())
                .imageUrl(createRequest.getImageUrl()) // 核心：保存
                .startTime(parseTime(createRequest.getStartTime()))
                .endTime(parseTime(createRequest.getEndTime()))
                .confidence(createRequest.getConfidence())
                .build();

        if (behaviorAnalysis.getStartTime() == null) behaviorAnalysis.setStartTime(LocalDateTime.now());

        if (!this.save(behaviorAnalysis)) throw new RuntimeException("创建失败");
        log.info("创建成功: behaviorId={}", behaviorAnalysis.getBehaviorId());
    }

    @Override
    public void updateBehaviorAnalysis(BehaviorAnalysisUpdateRequest updateRequest) {
        if (updateRequest.getBehaviorId() == null) throw new RuntimeException("ID不能为空");

        BehaviorAnalysis.BehaviorAnalysisBuilder builder = BehaviorAnalysis.builder();
        builder.behaviorId(updateRequest.getBehaviorId());

        if (updateRequest.getPetId() != null) builder.petId(updateRequest.getPetId());
        if (StringUtils.hasText(updateRequest.getBehaviorType())) builder.behaviorType(updateRequest.getBehaviorType());
        if (updateRequest.getConfidence() != null) builder.confidence(updateRequest.getConfidence());

        // 关键修复：允许更新或保留图片路径，避免编辑后丢失
        if (StringUtils.hasText(updateRequest.getImageUrl())) {
            builder.imageUrl(updateRequest.getImageUrl());
        }

        if (StringUtils.hasText(updateRequest.getStartTime())) {
            builder.startTime(parseTime(updateRequest.getStartTime()));
        }
        if (StringUtils.hasText(updateRequest.getEndTime())) {
            builder.endTime(parseTime(updateRequest.getEndTime()));
        }

        if (!this.updateById(builder.build())) throw new RuntimeException("更新失败");
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