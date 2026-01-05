package com.petcare.controller;

import com.petcare.common.Result;
import com.petcare.dto.VetConsultationCreateRequest;
import com.petcare.dto.VetConsultationPageResponse;
import com.petcare.dto.VetConsultationQueryRequest;
import com.petcare.dto.VetConsultationUpdateRequest;
import com.petcare.entity.VetConsultation;
import com.petcare.service.VetConsultationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 兽医咨询控制器
 */
@Slf4j
@RestController
@RequestMapping("/vet-consultations")
public class VetConsultationController {

    @Autowired
    private VetConsultationService vetConsultationService;

    /**
     * 分页查询兽医咨询
     */
    @GetMapping
    public Result<VetConsultationPageResponse> pageQuery(
            @RequestParam(required = false) Integer petId,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) Integer answeredBy,
            @RequestParam(required = false) Boolean answered,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        log.info("分页查询兽医咨询: petId={}, userId={}, page={}, size={}", petId, userId, page, size);
        
        try {
            VetConsultationQueryRequest queryRequest = VetConsultationQueryRequest.builder()
                    .petId(petId)
                    .userId(userId)
                    .answeredBy(answeredBy)
                    .answered(answered)
                    .startTime(startTime)
                    .endTime(endTime)
                    .keyword(keyword)
                    .page(page)
                    .size(size)
                    .build();
            
            VetConsultationPageResponse response = vetConsultationService.pageQuery(queryRequest);
            return Result.success(response);
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 根据ID查询兽医咨询详情
     */
    @GetMapping("/{consultId}")
    public Result<VetConsultation> getVetConsultationById(@PathVariable Integer consultId) {
        log.info("查询兽医咨询详情: {}", consultId);
        try {
            VetConsultation consultation = vetConsultationService.getVetConsultationById(consultId);
            return Result.success(consultation);
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 创建兽医咨询
     */
    @PostMapping
    public Result<String> createVetConsultation(@RequestBody VetConsultationCreateRequest createRequest, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未授权");
        }
        
        // 如果没有指定提问用户，使用当前登录用户
        if (createRequest.getUserId() == null) {
            createRequest.setUserId(userId);
        }
        
        log.info("创建兽医咨询: userId={}, petId={}", createRequest.getUserId(), createRequest.getPetId());
        try {
            vetConsultationService.createVetConsultation(createRequest);
            return Result.success("创建兽医咨询成功");
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 更新兽医咨询
     */
    @PutMapping("/{consultId}")
    public Result<String> updateVetConsultation(@PathVariable Integer consultId, 
                                              @RequestBody VetConsultationUpdateRequest updateRequest,
                                              HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未授权");
        }
        
        // 设置咨询ID
        updateRequest.setConsultId(consultId);
        
        log.info("更新兽医咨询: {}", consultId);
        try {
            vetConsultationService.updateVetConsultation(updateRequest);
            return Result.success("更新兽医咨询成功");
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 删除兽医咨询
     */
    @DeleteMapping("/{consultId}")
    public Result<String> deleteVetConsultation(@PathVariable Integer consultId, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未授权");
        }
        
        log.info("删除兽医咨询: {}", consultId);
        try {
            vetConsultationService.deleteVetConsultation(consultId);
            return Result.success("删除兽医咨询成功");
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 批量删除兽医咨询
     */
    @DeleteMapping("/batch")
    public Result<String> batchDeleteVetConsultations(@RequestBody List<Integer> consultIds, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未授权");
        }
        
        log.info("批量删除兽医咨询: {}", consultIds);
        try {
            vetConsultationService.batchDeleteVetConsultations(consultIds);
            return Result.success("批量删除兽医咨询成功");
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 兽医回答问题
     */
    @PostMapping("/{consultId}/answer")
    public Result<String> answerConsultation(@PathVariable Integer consultId, 
                                           @RequestBody Map<String, String> requestBody,
                                           HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未授权");
        }
        
        String answer = requestBody.get("answer");
        if (answer == null || answer.trim().isEmpty()) {
            return Result.fail(400, "回答内容不能为空");
        }
        
        log.info("兽医回答咨询: consultId={}, answeredBy={}", consultId, userId);
        try {
            vetConsultationService.answerConsultation(consultId, answer, userId);
            return Result.success("回答咨询成功");
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }
}
