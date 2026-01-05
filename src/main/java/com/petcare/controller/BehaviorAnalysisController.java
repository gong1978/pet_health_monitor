package com.petcare.controller;

import com.petcare.common.Result;
import com.petcare.dto.BehaviorAnalysisCreateRequest;
import com.petcare.dto.BehaviorAnalysisPageResponse;
import com.petcare.dto.BehaviorAnalysisQueryRequest;
import com.petcare.dto.BehaviorAnalysisUpdateRequest;
import com.petcare.entity.BehaviorAnalysis;
import com.petcare.service.BehaviorAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 行为识别控制器
 */
@Slf4j
@RestController
@RequestMapping("/behavior-analysis")
public class BehaviorAnalysisController {

    @Autowired
    private BehaviorAnalysisService behaviorAnalysisService;

    /**
     * 分页查询行为识别
     */
    @GetMapping
    public Result<BehaviorAnalysisPageResponse> pageQuery(
            @RequestParam(required = false) Integer petId,
            @RequestParam(required = false) String behaviorType,
            @RequestParam(required = false) Float minConfidence,
            @RequestParam(required = false) Float maxConfidence,
            @RequestParam(required = false) String startTimeFrom,
            @RequestParam(required = false) String startTimeTo,
            @RequestParam(required = false) String endTimeFrom,
            @RequestParam(required = false) String endTimeTo,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        log.info("分页查询行为识别: petId={}, behaviorType={}, page={}, size={}", 
                petId, behaviorType, page, size);
        
        try {
            BehaviorAnalysisQueryRequest queryRequest = BehaviorAnalysisQueryRequest.builder()
                    .petId(petId)
                    .behaviorType(behaviorType)
                    .minConfidence(minConfidence)
                    .maxConfidence(maxConfidence)
                    .startTimeFrom(startTimeFrom)
                    .startTimeTo(startTimeTo)
                    .endTimeFrom(endTimeFrom)
                    .endTimeTo(endTimeTo)
                    .page(page)
                    .size(size)
                    .build();
            
            BehaviorAnalysisPageResponse response = behaviorAnalysisService.pageQuery(queryRequest);
            return Result.success(response);
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 根据ID查询行为识别详情
     */
    @GetMapping("/{behaviorId}")
    public Result<BehaviorAnalysis> getBehaviorAnalysisById(@PathVariable Long behaviorId) {
        log.info("查询行为识别详情: {}", behaviorId);
        try {
            BehaviorAnalysis behaviorAnalysis = behaviorAnalysisService.getBehaviorAnalysisById(behaviorId);
            return Result.success(behaviorAnalysis);
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 创建行为识别
     */
    @PostMapping
    public Result<String> createBehaviorAnalysis(@RequestBody BehaviorAnalysisCreateRequest createRequest, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未授权");
        }
        
        log.info("创建行为识别: petId={}, behaviorType={}", createRequest.getPetId(), createRequest.getBehaviorType());
        try {
            behaviorAnalysisService.createBehaviorAnalysis(createRequest);
            return Result.success("创建行为识别成功");
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 更新行为识别
     */
    @PutMapping("/{behaviorId}")
    public Result<String> updateBehaviorAnalysis(@PathVariable Long behaviorId, 
                                               @RequestBody BehaviorAnalysisUpdateRequest updateRequest,
                                               HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未授权");
        }
        
        // 设置行为记录ID
        updateRequest.setBehaviorId(behaviorId);
        
        log.info("更新行为识别: {}", behaviorId);
        try {
            behaviorAnalysisService.updateBehaviorAnalysis(updateRequest);
            return Result.success("更新行为识别成功");
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 删除行为识别
     */
    @DeleteMapping("/{behaviorId}")
    public Result<String> deleteBehaviorAnalysis(@PathVariable Long behaviorId, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未授权");
        }
        
        log.info("删除行为识别: {}", behaviorId);
        try {
            behaviorAnalysisService.deleteBehaviorAnalysis(behaviorId);
            return Result.success("删除行为识别成功");
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 批量删除行为识别
     */
    @DeleteMapping("/batch")
    public Result<String> batchDeleteBehaviorAnalysis(@RequestBody List<Long> behaviorIds, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未授权");
        }
        
        log.info("批量删除行为识别: {}", behaviorIds);
        try {
            behaviorAnalysisService.batchDeleteBehaviorAnalysis(behaviorIds);
            return Result.success("批量删除行为识别成功");
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }
}
