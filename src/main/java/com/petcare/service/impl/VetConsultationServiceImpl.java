package com.petcare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petcare.dto.VetConsultationCreateRequest;
import com.petcare.dto.VetConsultationPageResponse;
import com.petcare.dto.VetConsultationQueryRequest;
import com.petcare.dto.VetConsultationUpdateRequest;
import com.petcare.entity.Pet;
import com.petcare.entity.User;
import com.petcare.entity.VetConsultation;
import com.petcare.mapper.VetConsultationMapper;
import com.petcare.service.PetService;
import com.petcare.service.UserService;
import com.petcare.service.VetConsultationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 兽医咨询服务实现
 */
@Service
public class VetConsultationServiceImpl extends ServiceImpl<VetConsultationMapper, VetConsultation> implements VetConsultationService {

    @Autowired
    private PetService petService;

    @Autowired
    private UserService userService;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public VetConsultationPageResponse pageQuery(VetConsultationQueryRequest queryRequest) {
        // 验证参数
        if (queryRequest.getPage() == null || queryRequest.getPage() < 1) {
            queryRequest.setPage(1);
        }
        if (queryRequest.getSize() == null || queryRequest.getSize() < 1) {
            queryRequest.setSize(10);
        }

        // 构建查询条件
        QueryWrapper<VetConsultation> queryWrapper = new QueryWrapper<>();

        if (queryRequest.getPetId() != null) {
            queryWrapper.eq("pet_id", queryRequest.getPetId());
        }
        if (queryRequest.getUserId() != null) {
            queryWrapper.eq("user_id", queryRequest.getUserId());
        }
        if (queryRequest.getAnsweredBy() != null) {
            queryWrapper.eq("answered_by", queryRequest.getAnsweredBy());
        }
        if (queryRequest.getAnswered() != null) {
            if (queryRequest.getAnswered()) {
                queryWrapper.isNotNull("answer").ne("answer", "");
            } else {
                queryWrapper.and(wrapper -> wrapper.isNull("answer").or().eq("answer", ""));
            }
        }
        if (StringUtils.hasText(queryRequest.getKeyword())) {
            queryWrapper.like("question", queryRequest.getKeyword());
        }
        if (StringUtils.hasText(queryRequest.getStartTime())) {
            try {
                LocalDateTime startTime = LocalDateTime.parse(queryRequest.getStartTime(), formatter);
                queryWrapper.ge("asked_at", startTime);
            } catch (DateTimeParseException e) {
                throw new RuntimeException("开始时间格式错误，请使用 yyyy-MM-dd HH:mm:ss 格式");
            }
        }
        if (StringUtils.hasText(queryRequest.getEndTime())) {
            try {
                LocalDateTime endTime = LocalDateTime.parse(queryRequest.getEndTime(), formatter);
                queryWrapper.le("asked_at", endTime);
            } catch (DateTimeParseException e) {
                throw new RuntimeException("结束时间格式错误，请使用 yyyy-MM-dd HH:mm:ss 格式");
            }
        }

        // 按提问时间倒序，未回答的在前
        queryWrapper.orderByAsc("CASE WHEN answer IS NULL OR answer = '' THEN 0 ELSE 1 END")
                .orderByDesc("asked_at");

        // 分页查询
        Page<VetConsultation> page = new Page<>(queryRequest.getPage(), queryRequest.getSize());
        page = this.page(page, queryWrapper);

        // 获取关联信息
        List<VetConsultation> records = page.getRecords();

        // [核心修复] 获取 Map 时确保即使为空也不会报错
        Map<Integer, String> petMap = getPetNameMap(records);
        Map<Integer, String> userMap = getUserNameMap(records);
        Map<Integer, String> vetMap = getVetNameMap(records);

        // 转换为响应DTO
        List<VetConsultationPageResponse.VetConsultationResponse> responseList = records.stream()
                .map(consult -> {
                    boolean answered = StringUtils.hasText(consult.getAnswer());

                    // [核心修复] 安全获取名称，防止 NPE
                    String petName = (consult.getPetId() != null && petMap.containsKey(consult.getPetId()))
                            ? petMap.get(consult.getPetId()) : "未知宠物";

                    String userName = (consult.getUserId() != null && userMap.containsKey(consult.getUserId()))
                            ? userMap.get(consult.getUserId()) : "未知用户";

                    String vetName = (consult.getAnsweredBy() != null && vetMap.containsKey(consult.getAnsweredBy()))
                            ? vetMap.get(consult.getAnsweredBy()) : "";

                    return VetConsultationPageResponse.VetConsultationResponse.builder()
                            .consultId(consult.getConsultId())
                            .petId(consult.getPetId())
                            .petName(petName)
                            .userId(consult.getUserId())
                            .userName(userName)
                            .question(consult.getQuestion())
                            .answer(consult.getAnswer())
                            .askedAt(consult.getAskedAt() != null ? consult.getAskedAt().format(formatter) : "")
                            .answeredBy(consult.getAnsweredBy())
                            .answeredByName(vetName)
                            .answered(answered)
                            .build();
                })
                .collect(Collectors.toList());

        return VetConsultationPageResponse.builder()
                .records(responseList)
                .total(page.getTotal())
                .current(page.getCurrent())
                .size(page.getSize())
                .pages(page.getPages())
                .build();
    }

    @Override
    public VetConsultation getVetConsultationById(Integer consultId) {
        if (consultId == null) {
            throw new RuntimeException("咨询ID不能为空");
        }

        VetConsultation consultation = this.getById(consultId);
        if (consultation == null) {
            throw new RuntimeException("兽医咨询记录不存在");
        }

        return consultation;
    }

    @Override
    public void createVetConsultation(VetConsultationCreateRequest createRequest) {
        // [核心修复] 严格参数校验，防止 NPE
        if (createRequest == null) {
            throw new RuntimeException("请求参数不能为空");
        }
        if (createRequest.getUserId() == null) {
            throw new RuntimeException("提问用户不能为空");
        }
        if (createRequest.getPetId() == null) {
            throw new RuntimeException("请选择咨询的宠物");
        }
        if (!StringUtils.hasText(createRequest.getQuestion())) {
            throw new RuntimeException("咨询问题不能为空");
        }

        // 验证宠物是否存在
        Pet pet = petService.getPetById(createRequest.getPetId());
        if (pet == null) {
            throw new RuntimeException("关联的宠物不存在");
        }

        // 验证提问用户是否存在
        User user = userService.getUserById(createRequest.getUserId());
        if (user == null) {
            throw new RuntimeException("提问用户不存在");
        }

        // 解析提问时间
        LocalDateTime askedAt;
        if (StringUtils.hasText(createRequest.getAskedAt())) {
            try {
                askedAt = LocalDateTime.parse(createRequest.getAskedAt(), formatter);
            } catch (DateTimeParseException e) {
                throw new RuntimeException("提问时间格式错误，请使用 yyyy-MM-dd HH:mm:ss 格式");
            }
        } else {
            askedAt = LocalDateTime.now();
        }

        // 构建实体
        VetConsultation consultation = VetConsultation.builder()
                .petId(createRequest.getPetId())
                .userId(createRequest.getUserId())
                .question(createRequest.getQuestion())
                .askedAt(askedAt)
                .answer(null)
                .answeredBy(null)
                .build();

        boolean success = this.save(consultation);
        if (!success) {
            throw new RuntimeException("创建兽医咨询记录失败");
        }
    }

    @Override
    public void updateVetConsultation(VetConsultationUpdateRequest updateRequest) {
        // 验证参数
        if (updateRequest.getConsultId() == null) {
            throw new RuntimeException("咨询ID不能为空");
        }

        // 检查记录是否存在
        VetConsultation existConsult = getVetConsultationById(updateRequest.getConsultId());
        if (existConsult == null) {
            throw new RuntimeException("兽医咨询记录不存在");
        }

        // 验证宠物是否存在
        if (updateRequest.getPetId() != null && !updateRequest.getPetId().equals(existConsult.getPetId())) {
            Pet pet = petService.getPetById(updateRequest.getPetId());
            if (pet == null) {
                throw new RuntimeException("关联的宠物不存在");
            }
        }

        // 验证提问用户是否存在
        if (updateRequest.getUserId() != null && !updateRequest.getUserId().equals(existConsult.getUserId())) {
            User user = userService.getUserById(updateRequest.getUserId());
            if (user == null) {
                throw new RuntimeException("提问用户不存在");
            }
        }

        // 验证回答兽医
        if (updateRequest.getAnsweredBy() != null) {
            User vet = userService.getUserById(updateRequest.getAnsweredBy());
            if (vet == null) {
                throw new RuntimeException("回答兽医不存在");
            }
            if (vet.getRole() == null || vet.getRole() != 2) {
                throw new RuntimeException("回答者必须是兽医角色");
            }
        }

        // 构建更新实体
        VetConsultation consultation = VetConsultation.builder()
                .consultId(updateRequest.getConsultId())
                .petId(updateRequest.getPetId())
                .userId(updateRequest.getUserId())
                .question(updateRequest.getQuestion())
                .answer(updateRequest.getAnswer())
                .answeredBy(updateRequest.getAnsweredBy())
                .build();

        boolean success = this.updateById(consultation);
        if (!success) {
            throw new RuntimeException("更新兽医咨询记录失败");
        }
    }

    @Override
    public void deleteVetConsultation(Integer consultId) {
        // 验证参数
        if (consultId == null) {
            throw new RuntimeException("咨询ID不能为空");
        }

        // 检查记录是否存在
        VetConsultation consultation = this.getById(consultId);
        if (consultation == null) {
            throw new RuntimeException("兽医咨询记录不存在");
        }

        boolean success = this.removeById(consultId);
        if (!success) {
            throw new RuntimeException("删除兽医咨询记录失败");
        }
    }

    @Override
    public void batchDeleteVetConsultations(List<Integer> consultIds) {
        // 验证参数
        if (consultIds == null || consultIds.isEmpty()) {
            throw new RuntimeException("咨询ID列表不能为空");
        }

        // 检查是否存在无效的ID
        for (Integer consultId : consultIds) {
            if (consultId == null) {
                throw new RuntimeException("咨询ID列表中包含空值");
            }
        }

        boolean success = this.removeByIds(consultIds);
        if (!success) {
            throw new RuntimeException("批量删除兽医咨询记录失败");
        }
    }

    @Override
    public void answerConsultation(Integer consultId, String answer, Integer vetId) {
        // 验证参数
        if (consultId == null) {
            throw new RuntimeException("咨询ID不能为空");
        }
        if (!StringUtils.hasText(answer)) {
            throw new RuntimeException("回答内容不能为空");
        }
        if (vetId == null) {
            throw new RuntimeException("回答兽医ID不能为空");
        }

        // 检查咨询记录是否存在
        VetConsultation existConsult = getVetConsultationById(consultId);
        if (existConsult == null) {
            throw new RuntimeException("兽医咨询记录不存在");
        }

        // 验证兽医身份
        User vet = userService.getUserById(vetId);
        if (vet == null) {
            throw new RuntimeException("回答兽医不存在");
        }
        if (vet.getRole() == null || vet.getRole() != 2) {
            throw new RuntimeException("回答者必须是兽医角色");
        }

        // 构建更新实体
        VetConsultation consultation = VetConsultation.builder()
                .consultId(consultId)
                .answer(answer)
                .answeredBy(vetId)
                .build();

        boolean success = this.updateById(consultation);
        if (!success) {
            throw new RuntimeException("回答咨询失败");
        }
    }

    /**
     * 获取宠物名称映射
     */
    private Map<Integer, String> getPetNameMap(List<VetConsultation> records) {
        List<Integer> petIds = records.stream()
                .map(VetConsultation::getPetId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        if (petIds.isEmpty()) {
            // [核心修复] 返回可变空 HashMap，防止 ImmutableMap 引发潜在问题
            return new java.util.HashMap<>();
        }

        List<Pet> pets = petService.listByIds(petIds);
        return pets.stream()
                .collect(Collectors.toMap(Pet::getPetId, pet -> pet.getName() != null ? pet.getName() : "未知"));
    }

    /**
     * 获取用户名称映射
     */
    private Map<Integer, String> getUserNameMap(List<VetConsultation> records) {
        List<Integer> userIds = records.stream()
                .map(VetConsultation::getUserId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        if (userIds.isEmpty()) {
            return new java.util.HashMap<>();
        }

        List<User> users = userService.listByIds(userIds);
        return users.stream()
                .collect(Collectors.toMap(User::getUserId,
                        user -> user.getFullName() != null ? user.getFullName() :
                                (user.getUsername() != null ? user.getUsername() : "未知")));
    }

    /**
     * 获取兽医名称映射
     */
    private Map<Integer, String> getVetNameMap(List<VetConsultation> records) {
        List<Integer> vetIds = records.stream()
                .map(VetConsultation::getAnsweredBy)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        if (vetIds.isEmpty()) {
            return new java.util.HashMap<>();
        }

        List<User> vets = userService.listByIds(vetIds);
        return vets.stream()
                .collect(Collectors.toMap(User::getUserId,
                        user -> user.getFullName() != null ? user.getFullName() :
                                (user.getUsername() != null ? user.getUsername() : "未知")));
    }
}