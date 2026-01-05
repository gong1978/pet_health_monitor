package com.petcare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcare.entity.UserPet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户宠物关联Mapper接口
 */
@Mapper
public interface UserPetMapper extends BaseMapper<UserPet> {
    
    /**
     * 根据用户ID查询其拥有的宠物ID列表
     */
    @Select("SELECT pet_id FROM user_pet WHERE user_id = #{userId}")
    List<Integer> getPetIdsByUserId(Integer userId);
    
    /**
     * 根据宠物ID查询其主人的用户ID列表
     */
    @Select("SELECT user_id FROM user_pet WHERE pet_id = #{petId}")
    List<Integer> getUserIdsByPetId(Integer petId);
    
    /**
     * 检查用户是否拥有指定宠物
     */
    @Select("SELECT COUNT(*) > 0 FROM user_pet WHERE user_id = #{userId} AND pet_id = #{petId}")
    boolean isUserPetOwner(Integer userId, Integer petId);
}
