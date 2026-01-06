package com.petcare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petcare.dto.AlertCreateRequest;
import com.petcare.dto.AlertPageResponse;
import com.petcare.dto.AlertQueryRequest;
import com.petcare.dto.AlertUpdateRequest;
import com.petcare.entity.Alert;
import com.petcare.entity.Pet;
import com.petcare.entity.User;
import com.petcare.mapper.AlertMapper;
import com.petcare.service.AlertService;
import com.petcare.service.PetService;
import com.petcare.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 异常预警服务实现
 */
@Slf4j
@Service
public class AlertServiceImpl extends ServiceImpl<AlertMapper, Alert> implements AlertService {

    @Autowired
    private PetService petService;

    @Autowired
    private UserService userService;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 有效的预警等级
    private final List<String> validLevels = Arrays.asList("warning", "critical");

    @Override
    public AlertPageResponse pageQuery(AlertQueryRequest queryRequest) {
        // 验证参数
        if (queryRequest.getPage() == null || queryRequest.getPage() < 1) {
            queryRequest.setPage(1);
        }
        if (queryRequest.getSize() == null || queryRequest.getSize() < 1) {
            queryRequest.setSize(10);
        }

        // 构建查询条件
        QueryWrapper<Alert> queryWrapper = new QueryWrapper<>();

        // ✅ 更安全的权限过滤：只要 userPetIds != null，就表示要按权限过滤
        if (queryRequest.getUserPetIds() != null) {
            if (queryRequest.getUserPetIds().isEmpty()) {
                // 用户没有任何宠物权限，直接返回空页，避免误查全表
                return AlertPageResponse.builder()
                        .records(List.of())
                        .total(0L)
                        .current(queryRequest.getPage().longValue())
                        .size(queryRequest.getSize().longValue())
                        .pages(0L)
                        .build();
            }
            queryWrapper.in("pet_id", queryRequest.getUserPetIds());
        }

        if (queryRequest.getPetId() != null) {
            queryWrapper.eq("pet_id", queryRequest.getPetId());
        }
        if (StringUtils.hasText(queryRequest.getAlertType())) {
            queryWrapper.eq("alert_type", queryRequest.getAlertType());
        }
        if (StringUtils.hasText(queryRequest.getLevel())) {
            queryWrapper.eq("level", queryRequest.getLevel());
        }

        // ✅ 关键修复：isResolved=false 时，把 is_resolved 为 NULL 的也当未处理
        if (queryRequest.getIsResolved() != null) {
            if (Boolean.FALSE.equals(queryRequest.getIsResolved())) {
                // 未处理：0 或 NULL
                queryWrapper.and(w -> w.eq("is_resolved", 0).or().isNull("is_resolved"));
            } else {
                // 已处理：1
                queryWrapper.eq("is_resolved", 1);
            }
        }

        if (queryRequest.getResolvedBy() != null) {
            queryWrapper.eq("resolved_by", queryRequest.getResolvedBy());
        }
        if (StringUtils.hasText(queryRequest.getStartTime())) {
            try {
                LocalDateTime startTime = LocalDateTime.parse(queryRequest.getStartTime(), formatter);
                queryWrapper.ge("created_at", startTime);
            } catch (DateTimeParseException e) {
                throw new RuntimeException("开始时间格式错误，请使用 yyyy-MM-dd HH:mm:ss 格式");
            }
        }
        if (StringUtils.hasText(queryRequest.getEndTime())) {
            try {
                LocalDateTime endTime = LocalDateTime.parse(queryRequest.getEndTime(), formatter);
                queryWrapper.le("created_at", endTime);
            } catch (DateTimeParseException e) {
                throw new RuntimeException("结束时间格式错误，请使用 yyyy-MM-dd HH:mm:ss 格式");
            }
        }

        // 按预警时间倒序，未处理的在前
        queryWrapper.orderByAsc("is_resolved").orderByDesc("created_at");

        // 分页查询
        Page<Alert> page = new Page<>(queryRequest.getPage(), queryRequest.getSize());
        page = this.page(page, queryWrapper);

        // 获取关联的宠物信息
        List<Alert> records = page.getRecords();
        Map<Integer, String> petNameMap = getPetNameMap(records);

        // 获取处理人信息映射
        Map<Integer, String> resolverNameMap = getResolverNameMap(records);

        // 转换为响应 DTO 列表
        List<AlertPageResponse.AlertResponse> responseList = records.stream()
                .map(alert -> {
                    Integer resolvedBy = alert.getResolvedBy();
                    Boolean resolved = alert.getIsResolved();

                    return AlertPageResponse.AlertResponse.builder()
                            .alertId(alert.getAlertId())
                            .petId(alert.getPetId())
                            .petName(petNameMap.getOrDefault(alert.getPetId(), "未知"))
                            .alertType(alert.getAlertType())
                            .alertMessage(alert.getAlertMessage())
                            .level(alert.getLevel())
                            .createdAt(alert.getCreatedAt() != null ? alert.getCreatedAt().format(formatter) : "")
                            // ✅ 关键兜底：null -> false（防止前端/图表/逻辑异常）
                            .isResolved(Boolean.TRUE.equals(resolved))
                            .resolvedBy(resolvedBy)
                            // ✅ resolvedBy 可能为 null，避免无意义 get(null)
                            .resolverName(resolvedBy == null ? null : resolverNameMap.get(resolvedBy))
                            .build();
                })
                .collect(Collectors.toList());

        // 返回封装对象
        return AlertPageResponse.builder()
                .records(responseList)
                .total(page.getTotal())
                .current(page.getCurrent())
                .size(page.getSize())
                .pages(page.getPages())
                .build();
    }

    @Override
    public Alert getAlertById(Integer alertId) {
        if (alertId == null) {
            throw new RuntimeException("预警ID不能为空");
        }

        Alert alert = this.getById(alertId);
        if (alert == null) {
            throw new RuntimeException("异常预警不存在");
        }

        return alert;
    }

    @Override
    public void createAlert(AlertCreateRequest createRequest) {
        // 验证参数
        if (createRequest.getPetId() == null) {
            throw new RuntimeException("宠物ID不能为空");
        }
        if (!StringUtils.hasText(createRequest.getAlertType())) {
            throw new RuntimeException("预警类型不能为空");
        }
        if (!StringUtils.hasText(createRequest.getAlertMessage())) {
            throw new RuntimeException("预警内容不能为空");
        }

        // 验证宠物是否存在
        Pet pet = petService.getPetById(createRequest.getPetId());
        if (pet == null) {
            throw new RuntimeException("关联的宠物不存在");
        }

        // 验证预警等级
        if (StringUtils.hasText(createRequest.getLevel()) && !validLevels.contains(createRequest.getLevel())) {
            throw new RuntimeException("预警等级只能是 warning 或 critical");
        }

        // 解析预警时间
        LocalDateTime createdAt;
        if (StringUtils.hasText(createRequest.getCreatedAt())) {
            try {
                createdAt = LocalDateTime.parse(createRequest.getCreatedAt(), formatter);
            } catch (DateTimeParseException e) {
                throw new RuntimeException("预警时间格式错误，请使用 yyyy-MM-dd HH:mm:ss 格式");
            }
        } else {
            createdAt = LocalDateTime.now();
        }

        // 构建实体
        Alert alert = Alert.builder()
                .petId(createRequest.getPetId())
                .alertType(createRequest.getAlertType())
                .alertMessage(createRequest.getAlertMessage())
                .level(createRequest.getLevel() != null ? createRequest.getLevel() : "warning")
                .createdAt(createdAt)
                // ✅ 明确写入 false，避免数据库出现 NULL
                .isResolved(false)
                .resolvedBy(null)
                .build();

        boolean success = this.save(alert);
        if (!success) {
            throw new RuntimeException("创建异常预警失败");
        }

        log.info("创建异常预警成功: petId={}, alertType={}, alertId={}",
                createRequest.getPetId(), createRequest.getAlertType(), alert.getAlertId());
    }

    @Override
    public void updateAlert(AlertUpdateRequest updateRequest) {
        // 验证参数
        if (updateRequest.getAlertId() == null) {
            throw new RuntimeException("预警ID不能为空");
        }

        // 检查预警是否存在
        Alert existAlert = getAlertById(updateRequest.getAlertId());
        if (existAlert == null) {
            throw new RuntimeException("异常预警不存在");
        }

        // 如果更新了宠物ID，验证宠物是否存在
        if (updateRequest.getPetId() != null && !updateRequest.getPetId().equals(existAlert.getPetId())) {
            Pet pet = petService.getPetById(updateRequest.getPetId());
            if (pet == null) {
                throw new RuntimeException("关联的宠物不存在");
            }
        }

        // 如果更新了处理用户ID，验证用户是否存在
        if (updateRequest.getResolvedBy() != null) {
            User resolver = userService.getUserById(updateRequest.getResolvedBy());
            if (resolver == null) {
                throw new RuntimeException("处理用户不存在");
            }
        }

        // 验证预警等级
        if (StringUtils.hasText(updateRequest.getLevel()) && !validLevels.contains(updateRequest.getLevel())) {
            throw new RuntimeException("预警等级只能是 warning 或 critical");
        }

        // 解析预警时间
        LocalDateTime createdAt = null;
        if (StringUtils.hasText(updateRequest.getCreatedAt())) {
            try {
                createdAt = LocalDateTime.parse(updateRequest.getCreatedAt(), formatter);
            } catch (DateTimeParseException e) {
                throw new RuntimeException("时间格式错误，请使用 yyyy-MM-dd HH:mm:ss");
            }
        }

        // 构建更新实体
        Alert alert = Alert.builder()
                .alertId(updateRequest.getAlertId())
                .petId(updateRequest.getPetId())
                .alertType(updateRequest.getAlertType())
                .alertMessage(updateRequest.getAlertMessage())
                .level(updateRequest.getLevel())
                .isResolved(updateRequest.getIsResolved())
                .resolvedBy(updateRequest.getResolvedBy())
                .createdAt(createdAt)
                .build();

        boolean success = this.updateById(alert);
        if (!success) {
            throw new RuntimeException("更新异常预警失败");
        }

        log.info("更新异常预警成功: alertId={}", updateRequest.getAlertId());
    }

    @Override
    public void deleteAlert(Integer alertId) {
        if (alertId == null) {
            throw new RuntimeException("预警ID不能为空");
        }

        Alert alert = this.getById(alertId);
        if (alert == null) {
            throw new RuntimeException("异常预警不存在");
        }

        boolean success = this.removeById(alertId);
        if (!success) {
            throw new RuntimeException("删除异常预警失败");
        }

        log.info("删除异常预警成功: alertId={}", alertId);
    }

    @Override
    public void batchDeleteAlerts(List<Integer> alertIds) {
        if (alertIds == null || alertIds.isEmpty()) {
            throw new RuntimeException("预警ID列表不能为空");
        }

        for (Integer alertId : alertIds) {
            if (alertId == null) {
                throw new RuntimeException("预警ID列表中包含空值");
            }
        }

        boolean success = this.removeByIds(alertIds);
        if (!success) {
            throw new RuntimeException("批量删除异常预警失败");
        }

        log.info("批量删除异常预警成功，删除数量: {}", alertIds.size());
    }

    @Override
    public void resolveAlert(Integer alertId, Integer userId) {
        if (alertId == null) {
            throw new RuntimeException("预警ID不能为空");
        }
        if (userId == null) {
            throw new RuntimeException("处理用户ID不能为空");
        }

        Alert existAlert = getAlertById(alertId);

        User resolver = userService.getUserById(userId);
        if (resolver == null) {
            throw new RuntimeException("处理用户不存在");
        }

        Alert alert = Alert.builder()
                .alertId(alertId)
                .isResolved(true)
                .resolvedBy(userId)
                .build();

        boolean success = this.updateById(alert);
        if (!success) {
            throw new RuntimeException("处理预警失败");
        }

        log.info("处理预警成功: alertId={}, resolvedBy={}", alertId, userId);
    }

    @Override
    public void batchResolveAlerts(List<Integer> alertIds, Integer userId) {
        if (alertIds == null || alertIds.isEmpty()) {
            throw new RuntimeException("预警ID列表不能为空");
        }
        if (userId == null) {
            throw new RuntimeException("处理用户ID不能为空");
        }

        User resolver = userService.getUserById(userId);
        if (resolver == null) {
            throw new RuntimeException("处理用户不存在");
        }

        for (Integer alertId : alertIds) {
            if (alertId != null) {
                Alert alert = Alert.builder()
                        .alertId(alertId)
                        .isResolved(true)
                        .resolvedBy(userId)
                        .build();
                this.updateById(alert);
            }
        }

        log.info("批量处理预警成功，处理数量: {}, resolvedBy: {}", alertIds.size(), userId);
    }

    /**
     * 获取宠物名称映射
     */
    private Map<Integer, String> getPetNameMap(List<Alert> records) {
        List<Integer> petIds = records.stream()
                .map(Alert::getPetId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        if (petIds.isEmpty()) {
            return Map.of();
        }

        List<Pet> pets = petService.listByIds(petIds);
        return pets.stream()
                .collect(Collectors.toMap(
                        Pet::getPetId,
                        pet -> pet.getName() != null ? pet.getName() : "未知"
                ));
    }

    /**
     * 获取处理用户名称映射
     */
    private Map<Integer, String> getResolverNameMap(List<Alert> records) {
        List<Integer> resolverIds = records.stream()
                .map(Alert::getResolvedBy)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        if (resolverIds.isEmpty()) {
            return Map.of();
        }

        List<User> resolvers = userService.listByIds(resolverIds);
        return resolvers.stream()
                .collect(Collectors.toMap(
                        User::getUserId,
                        user -> user.getFullName() != null
                                ? user.getFullName()
                                : (user.getUsername() != null ? user.getUsername() : "未知")
                ));
    }
}
