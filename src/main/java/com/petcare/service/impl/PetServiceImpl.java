package com.petcare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petcare.dto.PetCreateRequest;
import com.petcare.dto.PetPageResponse;
import com.petcare.dto.PetQueryRequest;
import com.petcare.dto.PetUpdateRequest;
import com.petcare.entity.Pet;
import com.petcare.mapper.PetMapper;
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

    @Override
    public PetPageResponse pageQuery(PetQueryRequest queryRequest) {
        // 验证参数
        if (queryRequest.getPage() == null || queryRequest.getPage() < 1) {
            queryRequest.setPage(1);
        }
        if (queryRequest.getSize() == null || queryRequest.getSize() < 1) {
            queryRequest.setSize(10);
        }

        // 构建查询条件
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
        
        // 按创建时间倒序
        queryWrapper.orderByDesc("created_at");

        // 分页查询
        Page<Pet> page = new Page<>(queryRequest.getPage(), queryRequest.getSize());
        page = this.page(page, queryWrapper);

        // 构建响应
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
        if (petId == null) {
            throw new RuntimeException("宠物ID不能为空");
        }

        Pet pet = this.getById(petId);
        if (pet == null) {
            throw new RuntimeException("宠物不存在");
        }
        
        return pet;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPet(PetCreateRequest createRequest, Integer userId) {
        // 验证参数
        if (userId == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        
        if (createRequest.getName() == null || createRequest.getName().trim().isEmpty()) {
            throw new RuntimeException("宠物名字不能为空");
        }
        
        if (createRequest.getSpecies() == null || createRequest.getSpecies().trim().isEmpty()) {
            throw new RuntimeException("宠物物种不能为空");
        }

        // 验证年龄
        if (createRequest.getAge() != null && createRequest.getAge() < 0) {
            throw new RuntimeException("宠物年龄不能为负数");
        }

        // 验证体重
        if (createRequest.getWeight() != null && createRequest.getWeight() <= 0) {
            throw new RuntimeException("宠物体重必须大于0");
        }

        // 构建实体
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

        boolean success = this.save(pet);
        if (!success) {
            throw new RuntimeException("创建宠物失败");
        }

        // 创建用户-宠物关联关系
        try {
            userPetService.addUserPetRelation(userId, pet.getPetId());
            log.info("创建宠物成功并建立用户关联: petId={}, petName={}, userId={}", 
                    pet.getPetId(), pet.getName(), userId);
        } catch (Exception e) {
            log.error("创建用户-宠物关联失败: petId={}, userId={}", pet.getPetId(), userId, e);
            throw new RuntimeException("创建宠物成功，但建立用户关联失败: " + e.getMessage());
        }
    }

    @Override
    public void updatePet(PetUpdateRequest updateRequest) {
        // 验证参数
        if (updateRequest.getPetId() == null) {
            throw new RuntimeException("宠物ID不能为空");
        }

        if (updateRequest.getName() == null || updateRequest.getName().trim().isEmpty()) {
            throw new RuntimeException("宠物名字不能为空");
        }

        if (updateRequest.getSpecies() == null || updateRequest.getSpecies().trim().isEmpty()) {
            throw new RuntimeException("宠物物种不能为空");
        }

        // 检查宠物是否存在
        Pet existPet = getPetById(updateRequest.getPetId());
        if (existPet == null) {
            throw new RuntimeException("宠物不存在");
        }

        // 验证年龄
        if (updateRequest.getAge() != null && updateRequest.getAge() < 0) {
            throw new RuntimeException("宠物年龄不能为负数");
        }

        // 验证体重
        if (updateRequest.getWeight() != null && updateRequest.getWeight() <= 0) {
            throw new RuntimeException("宠物体重必须大于0");
        }

        // 构建更新实体
        Pet pet = Pet.builder()
                .petId(updateRequest.getPetId())
                .name(updateRequest.getName().trim())
                .species(updateRequest.getSpecies().trim())
                .breed(updateRequest.getBreed() != null ? updateRequest.getBreed().trim() : null)
                .gender(updateRequest.getGender())
                .age(updateRequest.getAge())
                .weight(updateRequest.getWeight())
                .vaccineRecord(updateRequest.getVaccineRecord())
                .imageUrl(updateRequest.getImageUrl())
                .build();

        boolean success = this.updateById(pet);
        if (!success) {
            throw new RuntimeException("更新宠物信息失败");
        }

        log.info("更新宠物信息成功: {}", pet.getName());
    }

    @Override
    public void deletePet(Integer petId) {
        // 验证参数
        if (petId == null) {
            throw new RuntimeException("宠物ID不能为空");
        }

        // 检查宠物是否存在
        Pet pet = this.getById(petId);
        if (pet == null) {
            throw new RuntimeException("宠物不存在");
        }

        boolean success = this.removeById(petId);
        if (!success) {
            throw new RuntimeException("删除宠物失败");
        }

        log.info("删除宠物成功: {}", pet.getName());
    }

    @Override
    public void batchDeletePets(List<Integer> petIds) {
        // 验证参数
        if (petIds == null || petIds.isEmpty()) {
            throw new RuntimeException("宠物ID列表不能为空");
        }

        // 检查是否存在无效的ID
        for (Integer petId : petIds) {
            if (petId == null) {
                throw new RuntimeException("宠物ID列表中包含空值");
            }
        }

        boolean success = this.removeByIds(petIds);
        if (!success) {
            throw new RuntimeException("批量删除宠物失败");
        }

        log.info("批量删除宠物成功，删除数量: {}", petIds.size());
    }
}
