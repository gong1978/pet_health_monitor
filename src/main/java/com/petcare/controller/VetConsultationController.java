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
     * 修改点：增加权限判断，普通用户强制只能查自己的记录
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
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request
    ) {
        // 1. 获取当前登录用户信息
        Integer currentUserId = (Integer) request.getAttribute("userId");
        Integer role = (Integer) request.getAttribute("role");

        log.info("分页查询兽医咨询: userId={}, role={}, page={}, size={}", currentUserId, role, page, size);

        try {
            // 2. 构建查询对象
            VetConsultationQueryRequest queryRequest = VetConsultationQueryRequest.builder()
                    .petId(petId)
                    .userId(userId) // 初始使用前端传的参数
                    .answeredBy(answeredBy)
                    .answered(answered)
                    .startTime(startTime)
                    .endTime(endTime)
                    .keyword(keyword)
                    .page(page)
                    .size(size)
                    .build();

            // 3. 【核心修改】数据隔离逻辑
            // 如果是宠物主人 (Role = 3)，强制只能查询 user_id 为自己的记录
            if (role != null && role == 3) {
                queryRequest.setUserId(currentUserId);
            }

            VetConsultationPageResponse response = vetConsultationService.pageQuery(queryRequest);
            return Result.success(response);
        } catch (Exception e) {
            log.error("查询咨询列表失败", e);
            String errorMsg = e.getMessage() != null ? e.getMessage() : "查询失败";
            return Result.fail(400, errorMsg);
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
        } catch (Exception e) {
            String errorMsg = e.getMessage() != null ? e.getMessage() : "查询详情失败";
            return Result.fail(400, errorMsg);
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

        // 【修改点】强制绑定当前登录用户，防止代发
        createRequest.setUserId(userId);

        log.info("创建兽医咨询: userId={}, petId={}", userId, createRequest.getPetId());
        try {
            vetConsultationService.createVetConsultation(createRequest);
            return Result.success("创建兽医咨询成功");
        } catch (Exception e) {
            // [核心修复] 打印堆栈信息，并处理 message 为 null 的情况
            log.error("创建咨询失败", e);
            String errorMsg = e.getMessage() != null ? e.getMessage() : "系统内部错误(请检查必填项)";
            return Result.fail(400, errorMsg);
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
        } catch (Exception e) {
            log.error("更新咨询失败", e);
            String errorMsg = e.getMessage() != null ? e.getMessage() : "更新失败";
            return Result.fail(400, errorMsg);
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
        } catch (Exception e) {
            String errorMsg = e.getMessage() != null ? e.getMessage() : "删除失败";
            return Result.fail(400, errorMsg);
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
        } catch (Exception e) {
            String errorMsg = e.getMessage() != null ? e.getMessage() : "批量删除失败";
            return Result.fail(400, errorMsg);
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
        Integer role = (Integer) request.getAttribute("role");

        if (userId == null) {
            return Result.unauthorized("未授权");
        }

        // 【修改点】权限校验：只有管理员(1)或兽医(2)可以回答
        if (role == null || role == 3) {
            return Result.fail(403, "您没有权限回答咨询");
        }

        String answer = requestBody.get("answer");
        // 这里允许 answer 为 null 的情况在 service 层校验，或者在这里统一校验

        log.info("兽医回答咨询: consultId={}, answeredBy={}", consultId, userId);
        try {
            vetConsultationService.answerConsultation(consultId, answer, userId);
            return Result.success("回答咨询成功");
        } catch (Exception e) {
            log.error("回答咨询失败", e);
            String errorMsg = e.getMessage() != null ? e.getMessage() : "回答失败";
            return Result.fail(400, errorMsg);
        }
    }
}