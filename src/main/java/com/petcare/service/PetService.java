package com.petcare.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.petcare.dto.PetCreateRequest;
import com.petcare.dto.PetPageResponse;
import com.petcare.dto.PetQueryRequest;
import com.petcare.dto.PetUpdateRequest;
import com.petcare.entity.Pet;

import java.util.List;

/**
 * 宠物服务接口
 */
public interface PetService extends IService<Pet> {
    /**
     * 分页查询宠物
     */
    PetPageResponse pageQuery(PetQueryRequest queryRequest);

    /**
     * 根据宠物ID查询宠物
     */
    Pet getPetById(Integer petId);

    /**
     * 创建宠物
     * @param createRequest 宠物创建请求
     * @param userId 创建宠物的用户ID（用于建立关联）
     */
    void createPet(PetCreateRequest createRequest, Integer userId);

    /**
     * 更新宠物信息
     */
    void updatePet(PetUpdateRequest updateRequest);

    /**
     * 删除宠物
     */
    void deletePet(Integer petId);

    /**
     * 批量删除宠物
     */
    void batchDeletePets(List<Integer> petIds);
}
