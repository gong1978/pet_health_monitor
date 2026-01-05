package com.petcare.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.petcare.entity.UserPet;

import java.util.List;

/**
 * 用户宠物关联服务接口
 */
public interface UserPetService extends IService<UserPet> {
    
    /**
     * 为用户添加宠物关联
     */
    void addUserPetRelation(Integer userId, Integer petId);
    
    /**
     * 移除用户宠物关联
     */
    void removeUserPetRelation(Integer userId, Integer petId);
    
    /**
     * 根据用户ID获取其拥有的宠物ID列表
     */
    List<Integer> getPetIdsByUserId(Integer userId);
    
    /**
     * 根据宠物ID获取其主人的用户ID列表
     */
    List<Integer> getUserIdsByPetId(Integer petId);
    
    /**
     * 检查用户是否拥有指定宠物
     */
    boolean isUserPetOwner(Integer userId, Integer petId);
    
    /**
     * 批量为用户添加宠物关联
     */
    void addUserPetRelations(Integer userId, List<Integer> petIds);
    
    /**
     * 移除用户的所有宠物关联
     */
    void removeAllUserPetRelations(Integer userId);
    
    /**
     * 移除宠物的所有用户关联
     */
    void removeAllPetUserRelations(Integer petId);
}
