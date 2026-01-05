package com.petcare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petcare.dto.PetCreateRequest;
import com.petcare.dto.PetPageResponse;
import com.petcare.dto.PetQueryRequest;
import com.petcare.dto.PetUpdateRequest;
import com.petcare.entity.*;
import com.petcare.mapper.*;
import com.petcare.service.PetService;
import com.petcare.service.UserPetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 宠物服务实现
 */
@Slf4j
@Service
public class PetServiceImpl extends ServiceImpl<PetMapper, Pet> implements PetService {

    @Autowired
    private UserPetService userPetService;

    // 注入所有相关的 Mapper，用于级联删除
    @Autowired private UserPetMapper userPetMapper;
    @Autowired private SensorDataMapper sensorDataMapper;
    @Autowired private BehaviorAnalysisMapper behaviorAnalysisMapper;
    @Autowired private HealthReportMapper healthReportMapper;
    @Autowired private AlertMapper alertMapper;
    @Autowired private VetConsultationMapper vetConsultationMapper;

    @Override
    public PetPageResponse pageQuery(PetQueryRequest queryRequest) {
        if (queryRequest.getPage() == null || queryRequest.getPage() < 1) {
            queryRequest.setPage(1);
        }
        if (queryRequest.getSize() == null || queryRequest.getSize() < 1) {
            queryRequest.setSize(10);
        }

        QueryWrapper<Pet> queryWrapper = new QueryWrapper<>();
        if (queryRequest.getName() != null && !queryRequest.getName().isEmpty()) {
            queryWrapper.like("name", queryRequest.getName());
        }
        if (queryRequest.getSpecies() != null && !queryRequest.getSpecies().isEmpty()) {
            queryWrapper.eq("species", queryRequest.getSpecies());
        }
        if (queryRequest.getBreed() != null && !queryRequest.getBreed().isEmpty()) {
            queryWrapper.eq("breed", queryRequest.getBreed());
        }
        if (queryRequest.getGender() != null && !queryRequest.getGender().isEmpty()) {
            queryWrapper.eq("gender", queryRequest.getGender());
        }

        queryWrapper.orderByDesc("created_at");

        Page<Pet> page = new Page<>(queryRequest.getPage(), queryRequest.getSize());
        page = this.page(page, queryWrapper);

        return PetPageResponse.builder()
                .records(page.getRecords())
                .total(page.getTotal())
                .current(page.getCurrent())
                .size(page.getSize())
                .pages(page.getPages())
                .build();
    }

    @Override
    public Pet getPetById(Integer petId) {
        if (petId == null) throw new RuntimeException("宠物ID不能为空");
        Pet pet = this.getById(petId);
        if (pet == null) throw new RuntimeException("宠物不存在");
        return pet;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPet(PetCreateRequest createRequest, Integer userId) {
        if (userId == null) throw new RuntimeException("用户ID不能为空");
        if (createRequest.getName() == null || createRequest.getName().trim().isEmpty()) {
            throw new RuntimeException("宠物名字不能为空");
        }
        if (createRequest.getSpecies() == null || createRequest.getSpecies().trim().isEmpty()) {
            throw new RuntimeException("宠物物种不能为空");
        }

        Pet pet = Pet.builder()
                .name(createRequest.getName().trim())
                .species(createRequest.getSpecies().trim())
                .breed(createRequest.getBreed() != null ? createRequest.getBreed().trim() : null)
                .gender(createRequest.getGender())
                .age(createRequest.getAge())
                .weight(createRequest.getWeight())
                .vaccineRecord(createRequest.getVaccineRecord())
                .imageUrl(createRequest.getImageUrl())
                .createdAt(LocalDateTime.now())
                .build();

        if (!this.save(pet)) {
            throw new RuntimeException("创建宠物失败");
        }

        try {
            userPetService.addUserPetRelation(userId, pet.getPetId());
            log.info("创建宠物成功并建立用户关联: petId={}, userId={}", pet.getPetId(), userId);
        } catch (Exception e) {
            log.error("建立关联失败", e);
            throw new RuntimeException("创建宠物成功，但建立用户关联失败: " + e.getMessage());
        }
    }

    @Override
    public void updatePet(PetUpdateRequest updateRequest) {
        if (updateRequest.getPetId() == null) throw new RuntimeException("宠物ID不能为空");

        Pet existPet = getPetById(updateRequest.getPetId()); // 检查存在性

        Pet pet = Pet.builder()
                .petId(updateRequest.getPetId())
                .name(updateRequest.getName() != null ? updateRequest.getName().trim() : existPet.getName())
                .species(updateRequest.getSpecies() != null ? updateRequest.getSpecies().trim() : existPet.getSpecies())
                .breed(updateRequest.getBreed())
                .gender(updateRequest.getGender())
                .age(updateRequest.getAge())
                .weight(updateRequest.getWeight())
                .vaccineRecord(updateRequest.getVaccineRecord())
                .imageUrl(updateRequest.getImageUrl())
                .build();

        if (!this.updateById(pet)) {
            throw new RuntimeException("更新宠物信息失败");
        }
        log.info("更新宠物信息成功: {}", pet.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePet(Integer petId) {
        if (petId == null) throw new RuntimeException("宠物ID不能为空");

        // 检查是否存在
        Pet pet = this.getById(petId);
        if (pet == null) throw new RuntimeException("宠物不存在");

        log.info("开始级联删除宠物数据: petId={}", petId);

        try {
            // 级联删除关联数据（触发逻辑删除 update ... set deleted=1）
            userPetMapper.delete(new LambdaQueryWrapper<UserPet>().eq(UserPet::getPetId, petId));
            sensorDataMapper.delete(new LambdaQueryWrapper<SensorData>().eq(SensorData::getPetId, petId));
            behaviorAnalysisMapper.delete(new LambdaQueryWrapper<BehaviorAnalysis>().eq(BehaviorAnalysis::getPetId, petId));
            healthReportMapper.delete(new LambdaQueryWrapper<HealthReport>().eq(HealthReport::getPetId, petId));
            alertMapper.delete(new LambdaQueryWrapper<Alert>().eq(Alert::getPetId, petId));
            vetConsultationMapper.delete(new LambdaQueryWrapper<VetConsultation>().eq(VetConsultation::getPetId, petId));

            // 删除宠物主体
            // 注意：如果 Pet 实体没有 @TableLogic，这里是物理删除，可能会因为子表（逻辑删除）仍有 FK 引用而报错
            // 建议给 Pet 实体加上 @TableLogic，或者确保子表也是物理删除
            boolean success = this.removeById(petId);
            if (!success) {
                throw new RuntimeException("删除宠物主数据失败");
            }
            log.info("删除宠物成功: {}", pet.getName());
        } catch (Exception e) {
            // 这里的异常会被 GlobalExceptionHandler 捕获并处理成友好提示
            log.error("删除失败", e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeletePets(List<Integer> petIds) {
        if (petIds == null || petIds.isEmpty()) return;
        for (Integer id : petIds) {
            deletePet(id); // 复用单个删除逻辑
        }
        log.info("批量删除完成，数量: {}", petIds.size());
    }
}