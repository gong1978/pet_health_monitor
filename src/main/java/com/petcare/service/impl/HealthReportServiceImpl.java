package com.petcare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petcare.dto.HealthReportCreateRequest;
import com.petcare.dto.HealthReportPageResponse;
import com.petcare.dto.HealthReportQueryRequest;
import com.petcare.dto.HealthReportUpdateRequest;
import com.petcare.entity.HealthReport;
import com.petcare.entity.Pet;
import com.petcare.entity.User;
import com.petcare.mapper.HealthReportMapper;
import com.petcare.service.HealthReportService;
import com.petcare.service.PetService;
import com.petcare.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 健康报告服务实现
 */
@Slf4j
@Service
public class HealthReportServiceImpl extends ServiceImpl<HealthReportMapper, HealthReport> implements HealthReportService {

    @Autowired
    private PetService petService;

    @Autowired
    private UserService userService;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public HealthReportPageResponse pageQuery(HealthReportQueryRequest queryRequest) {
        // 验证参数
        if (queryRequest.getPage() == null || queryRequest.getPage() < 1) {
            queryRequest.setPage(1);
        }
        if (queryRequest.getSize() == null || queryRequest.getSize() < 1) {
            queryRequest.setSize(10);
        }

        // 构建查询条件
        QueryWrapper<HealthReport> queryWrapper = new QueryWrapper<>();
        
        if (queryRequest.getPetId() != null) {
            queryWrapper.eq("pet_id", queryRequest.getPetId());
        }
        if (queryRequest.getMinHealthScore() != null) {
            queryWrapper.ge("health_score", queryRequest.getMinHealthScore());
        }
        if (queryRequest.getMaxHealthScore() != null) {
            queryWrapper.le("health_score", queryRequest.getMaxHealthScore());
        }
        if (queryRequest.getReviewedBy() != null) {
            queryWrapper.eq("reviewed_by", queryRequest.getReviewedBy());
        }
        if (StringUtils.hasText(queryRequest.getStartDate())) {
            try {
                LocalDate startDate = LocalDate.parse(queryRequest.getStartDate(), dateFormatter);
                queryWrapper.ge("report_date", startDate);
            } catch (DateTimeParseException e) {
                throw new RuntimeException("开始日期格式错误，请使用 yyyy-MM-dd 格式");
            }
        }
        if (StringUtils.hasText(queryRequest.getEndDate())) {
            try {
                LocalDate endDate = LocalDate.parse(queryRequest.getEndDate(), dateFormatter);
                queryWrapper.le("report_date", endDate);
            } catch (DateTimeParseException e) {
                throw new RuntimeException("结束日期格式错误，请使用 yyyy-MM-dd 格式");
            }
        }
        
        // 按报告日期倒序
        queryWrapper.orderByDesc("report_date");

        // 分页查询
        Page<HealthReport> page = new Page<>(queryRequest.getPage(), queryRequest.getSize());
        page = this.page(page, queryWrapper);

        // 获取关联的宠物和用户信息
        List<HealthReport> records = page.getRecords();
        Map<Integer, String> petNameMap = getPetNameMap(records);
        Map<Integer, String> reviewerNameMap = getReviewerNameMap(records);

        // 转换为响应DTO
        List<HealthReportPageResponse.HealthReportResponse> responseList = records.stream()
                .map(report -> HealthReportPageResponse.HealthReportResponse.builder()
                        .reportId(report.getReportId())
                        .petId(report.getPetId())
                        .petName(petNameMap.get(report.getPetId()))
                        .reportDate(report.getReportDate() != null ? report.getReportDate().format(dateFormatter) : "")
                        .healthScore(report.getHealthScore())
                        .summary(report.getSummary())
                        .suggestions(report.getSuggestions())
                        .reviewedBy(report.getReviewedBy())
                        .reviewerName(reviewerNameMap.get(report.getReviewedBy()))
                        .build())
                .collect(Collectors.toList());

        // 构建响应
        return HealthReportPageResponse.builder()
                .records(responseList)
                .total(page.getTotal())
                .current(page.getCurrent())
                .size(page.getSize())
                .pages(page.getPages())
                .build();
    }

    @Override
    public HealthReport getHealthReportById(Integer reportId) {
        if (reportId == null) {
            throw new RuntimeException("报告ID不能为空");
        }

        HealthReport healthReport = this.getById(reportId);
        if (healthReport == null) {
            throw new RuntimeException("健康报告不存在");
        }
        
        return healthReport;
    }

    @Override
    public void createHealthReport(HealthReportCreateRequest createRequest) {
        // 验证参数
        if (createRequest.getPetId() == null) {
            throw new RuntimeException("宠物ID不能为空");
        }

        // 验证宠物是否存在
        Pet pet = petService.getPetById(createRequest.getPetId());
        if (pet == null) {
            throw new RuntimeException("关联的宠物不存在");
        }

        // 验证审核兽医
        if (createRequest.getReviewedBy() != null) {
            User reviewer = userService.getUserById(createRequest.getReviewedBy());
            if (reviewer == null) {
                throw new RuntimeException("审核兽医不存在");
            }
            if (reviewer.getRole() == null || reviewer.getRole() != 2) {
                throw new RuntimeException("审核人必须是兽医角色");
            }
        }

        // 验证健康评分范围
        if (createRequest.getHealthScore() != null && 
            (createRequest.getHealthScore() < 0 || createRequest.getHealthScore() > 100)) {
            throw new RuntimeException("健康评分必须在0-100之间");
        }

        // 解析报告日期
        LocalDate reportDate;
        if (StringUtils.hasText(createRequest.getReportDate())) {
            try {
                reportDate = LocalDate.parse(createRequest.getReportDate(), dateFormatter);
            } catch (DateTimeParseException e) {
                throw new RuntimeException("报告日期格式错误，请使用 yyyy-MM-dd 格式");
            }
        } else {
            reportDate = LocalDate.now();
        }

        // 构建实体
        HealthReport healthReport = HealthReport.builder()
                .petId(createRequest.getPetId())
                .reportDate(reportDate)
                .healthScore(createRequest.getHealthScore())
                .summary(createRequest.getSummary())
                .suggestions(createRequest.getSuggestions())
                .reviewedBy(createRequest.getReviewedBy())
                .build();

        boolean success = this.save(healthReport);
        if (!success) {
            throw new RuntimeException("创建健康报告失败");
        }

        log.info("创建健康报告成功: petId={}, reportId={}", createRequest.getPetId(), healthReport.getReportId());
    }

    @Override
    public void updateHealthReport(HealthReportUpdateRequest updateRequest) {
        // 验证参数
        if (updateRequest.getReportId() == null) {
            throw new RuntimeException("报告ID不能为空");
        }

        // 检查报告是否存在
        HealthReport existReport = getHealthReportById(updateRequest.getReportId());
        if (existReport == null) {
            throw new RuntimeException("健康报告不存在");
        }

        // 如果更新了宠物ID，验证宠物是否存在
        if (updateRequest.getPetId() != null && !updateRequest.getPetId().equals(existReport.getPetId())) {
            Pet pet = petService.getPetById(updateRequest.getPetId());
            if (pet == null) {
                throw new RuntimeException("关联的宠物不存在");
            }
        }

        // 验证审核兽医
        if (updateRequest.getReviewedBy() != null) {
            User reviewer = userService.getUserById(updateRequest.getReviewedBy());
            if (reviewer == null) {
                throw new RuntimeException("审核兽医不存在");
            }
            if (reviewer.getRole() == null || reviewer.getRole() != 2) {
                throw new RuntimeException("审核人必须是兽医角色");
            }
        }

        // 验证健康评分范围
        if (updateRequest.getHealthScore() != null && 
            (updateRequest.getHealthScore() < 0 || updateRequest.getHealthScore() > 100)) {
            throw new RuntimeException("健康评分必须在0-100之间");
        }

        // 解析报告日期
        LocalDate reportDate = null;
        if (StringUtils.hasText(updateRequest.getReportDate())) {
            try {
                reportDate = LocalDate.parse(updateRequest.getReportDate(), dateFormatter);
            } catch (DateTimeParseException e) {
                throw new RuntimeException("报告日期格式错误，请使用 yyyy-MM-dd 格式");
            }
        }

        // 构建更新实体
        HealthReport healthReport = HealthReport.builder()
                .reportId(updateRequest.getReportId())
                .petId(updateRequest.getPetId())
                .reportDate(reportDate)
                .healthScore(updateRequest.getHealthScore())
                .summary(updateRequest.getSummary())
                .suggestions(updateRequest.getSuggestions())
                .reviewedBy(updateRequest.getReviewedBy())
                .build();

        boolean success = this.updateById(healthReport);
        if (!success) {
            throw new RuntimeException("更新健康报告失败");
        }

        log.info("更新健康报告成功: reportId={}", updateRequest.getReportId());
    }

    @Override
    public void deleteHealthReport(Integer reportId) {
        // 验证参数
        if (reportId == null) {
            throw new RuntimeException("报告ID不能为空");
        }

        // 检查报告是否存在
        HealthReport healthReport = this.getById(reportId);
        if (healthReport == null) {
            throw new RuntimeException("健康报告不存在");
        }

        boolean success = this.removeById(reportId);
        if (!success) {
            throw new RuntimeException("删除健康报告失败");
        }

        log.info("删除健康报告成功: reportId={}", reportId);
    }

    @Override
    public void batchDeleteHealthReports(List<Integer> reportIds) {
        // 验证参数
        if (reportIds == null || reportIds.isEmpty()) {
            throw new RuntimeException("报告ID列表不能为空");
        }

        // 检查是否存在无效的ID
        for (Integer reportId : reportIds) {
            if (reportId == null) {
                throw new RuntimeException("报告ID列表中包含空值");
            }
        }

        boolean success = this.removeByIds(reportIds);
        if (!success) {
            throw new RuntimeException("批量删除健康报告失败");
        }

        log.info("批量删除健康报告成功，删除数量: {}", reportIds.size());
    }

    /**
     * 获取宠物名称映射
     */
    private Map<Integer, String> getPetNameMap(List<HealthReport> records) {
        List<Integer> petIds = records.stream()
                .map(HealthReport::getPetId)
                .distinct()
                .collect(Collectors.toList());

        if (petIds.isEmpty()) {
            return Map.of();
        }

        List<Pet> pets = petService.listByIds(petIds);
        return pets.stream()
                .collect(Collectors.toMap(Pet::getPetId, pet -> pet.getName() != null ? pet.getName() : "未知"));
    }

    /**
     * 获取审核兽医名称映射
     */
    private Map<Integer, String> getReviewerNameMap(List<HealthReport> records) {
        List<Integer> reviewerIds = records.stream()
                .map(HealthReport::getReviewedBy)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        if (reviewerIds.isEmpty()) {
            return Map.of();
        }

        List<User> reviewers = userService.listByIds(reviewerIds);
        return reviewers.stream()
                .collect(Collectors.toMap(User::getUserId, 
                    user -> user.getFullName() != null ? user.getFullName() : 
                           (user.getUsername() != null ? user.getUsername() : "未知")));
    }
}
