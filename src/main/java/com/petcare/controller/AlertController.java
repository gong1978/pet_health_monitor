package com.petcare.controller;

import com.petcare.common.Result;
import com.petcare.dto.AlertCreateRequest;
import com.petcare.dto.AlertPageResponse;
import com.petcare.dto.AlertQueryRequest;
import com.petcare.dto.AlertUpdateRequest;
import com.petcare.entity.Alert;
import com.petcare.service.AlertService;
import com.petcare.service.UserPetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 异常预警控制器
 */
@Slf4j
@RestController
@RequestMapping("/alerts") // 注意：这里通常对应前端 api/alert.js 中的路径，保持复数形式与前端一致
public class AlertController {

    @Autowired
    private AlertService alertService;

    @Autowired
    private UserPetService userPetService;

    /**
     * 分页查询异常预警
     */
    @GetMapping
    public Result<AlertPageResponse> pageQuery(
            @RequestParam(required = false) Integer petId,
            @RequestParam(required = false) String alertType,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) Boolean isResolved,
            @RequestParam(required = false) Integer resolvedBy,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request
    ) {
        Integer userId = (Integer) request.getAttribute("userId");
        Integer userRole = (Integer) request.getAttribute("role");

        if (userId == null) {
            return Result.unauthorized("未授权");
        }

        log.info("分页查询异常预警: userId={}, role={}, petId={}, alertType={}, page={}, size={}",
                userId, userRole, petId, alertType, page, size);

        try {
            AlertQueryRequest queryRequest = AlertQueryRequest.builder()
                    .petId(petId)
                    .alertType(alertType)
                    .level(level)
                    .isResolved(isResolved)
                    .resolvedBy(resolvedBy)
                    .startTime(startTime)
                    .endTime(endTime)
                    .page(page)
                    .size(size)
                    .build();

            // ✅ 加在这里：打印 request attribute 实际类型，确认 role 有没有塞对
            log.info("alerts pageQuery attr: userId={}, role={}, roleClass={}",
                    userId, userRole,
                    (request.getAttribute("role") == null ? null : request.getAttribute("role").getClass()));

            // 如果是宠物主人(role=3)，只能查看自己宠物的预警
            if (userRole != null && userRole == 3) {
                // 获取该用户拥有的宠物ID列表
                List<Integer> userPetIds = userPetService.getPetIdsByUserId(userId);

                // ✅ 加在这里：看看多宠物用户实际拿到了哪些 petId
                log.info("owner filter userPetIds={}", userPetIds);

                if (userPetIds == null || userPetIds.isEmpty()) {
                    // 用户没有宠物，返回空结果，避免查询所有数据
                    return Result.success(AlertPageResponse.builder()
                            .records(List.of())
                            .total(0L)
                            .current((long) page)
                            .size((long) size)
                            .pages(0L)
                            .build());
                }

                // 如果请求中指定了petId，需要验证该宠物是否属于当前用户
                if (petId != null && !userPetIds.contains(petId)) {
                    return Result.fail(403, "无权查看该宠物的预警信息");
                }

                // 设置用户的宠物ID列表，用于 Service 层过滤
                queryRequest.setUserPetIds(userPetIds);
            }

            AlertPageResponse response = alertService.pageQuery(queryRequest);

            // ✅ 加这个：看后端到底返回了多少条、都属于哪些 petId
            log.info("alerts pageQuery result: total={}, recordSize={}, petIds={}",
                    response.getTotal(),
                    response.getRecords() == null ? 0 : response.getRecords().size(),
                    response.getRecords() == null ? List.of() :
                            response.getRecords().stream()
                                    .map(AlertPageResponse.AlertResponse::getPetId)
                                    .distinct()
                                    .toList()
            );

            return Result.success(response);
        } catch (RuntimeException e) {
            log.error("alerts pageQuery failed", e);
            return Result.fail(400, e.getMessage() == null ? "服务器内部错误，请查看后端日志" : e.getMessage());
        }
    }

    /**
     * 根据ID查询异常预警详情
     */
    @GetMapping("/{alertId}")
    public Result<Alert> getAlertById(@PathVariable Integer alertId) {
        log.info("查询异常预警详情: {}", alertId);
        try {
            Alert alert = alertService.getAlertById(alertId);
            return Result.success(alert);
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 创建异常预警
     */
    @PostMapping
    public Result<String> createAlert(@RequestBody AlertCreateRequest createRequest, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未授权");
        }

        log.info("创建异常预警: petId={}, alertType={}", createRequest.getPetId(), createRequest.getAlertType());
        try {
            alertService.createAlert(createRequest);
            return Result.success("创建异常预警成功");
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 更新异常预警
     */
    @PutMapping("/{alertId}")
    public Result<String> updateAlert(@PathVariable Integer alertId,
                                      @RequestBody AlertUpdateRequest updateRequest,
                                      HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未授权");
        }

        // 设置预警ID
        updateRequest.setAlertId(alertId);

        log.info("更新异常预警: {}", alertId);
        try {
            alertService.updateAlert(updateRequest);
            return Result.success("更新异常预警成功");
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 删除异常预警
     */
    @DeleteMapping("/{alertId}")
    public Result<String> deleteAlert(@PathVariable Integer alertId, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未授权");
        }

        log.info("删除异常预警: {}", alertId);
        try {
            alertService.deleteAlert(alertId);
            return Result.success("删除异常预警成功");
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 批量删除异常预警
     */
    @DeleteMapping("/batch")
    public Result<String> batchDeleteAlerts(@RequestBody List<Integer> alertIds, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未授权");
        }

        log.info("批量删除异常预警: {}", alertIds);
        try {
            alertService.batchDeleteAlerts(alertIds);
            return Result.success("批量删除异常预警成功");
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 处理预警（标记为已处理）
     */
    @PostMapping("/{alertId}/resolve")
    public Result<String> resolveAlert(@PathVariable Integer alertId, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未授权");
        }

        log.info("处理预警: alertId={}, resolvedBy={}", alertId, userId);
        try {
            alertService.resolveAlert(alertId, userId);
            return Result.success("处理预警成功");
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 批量处理预警
     */
    @PostMapping("/batch-resolve")
    public Result<String> batchResolveAlerts(@RequestBody List<Integer> alertIds, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未授权");
        }

        log.info("批量处理预警: alertIds={}, resolvedBy={}", alertIds, userId);
        try {
            alertService.batchResolveAlerts(alertIds, userId);
            return Result.success("批量处理预警成功");
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }
}