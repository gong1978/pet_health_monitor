package com.petcare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petcare.entity.UserPet;
import com.petcare.mapper.UserPetMapper;
import com.petcare.service.UserPetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户宠物关联服务实现
 */
@Slf4j
@Service
public class UserPetServiceImpl extends ServiceImpl<UserPetMapper, UserPet> implements UserPetService {

    @Override
    @Transactional
    public void addUserPetRelation(Integer userId, Integer petId) {
        // 验证参数
        if (userId == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        if (petId == null) {
            throw new RuntimeException("宠物ID不能为空");
        }

        // 检查关联是否已存在
        QueryWrapper<UserPet> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("pet_id", petId);
        UserPet existing = this.getOne(queryWrapper);
        
        if (existing != null) {
            log.warn("用户宠物关联已存在: userId={}, petId={}", userId, petId);
            return;
        }

        // 创建关联
        UserPet userPet = UserPet.builder()
                .userId(userId)
                .petId(petId)
                .build();

        boolean success = this.save(userPet);
        if (!success) {
            throw new RuntimeException("创建用户宠物关联失败");
        }

        log.info("创建用户宠物关联成功: userId={}, petId={}", userId, petId);
    }

    @Override
    @Transactional
    public void removeUserPetRelation(Integer userId, Integer petId) {
        // 验证参数
        if (userId == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        if (petId == null) {
            throw new RuntimeException("宠物ID不能为空");
        }

        // 删除关联
        QueryWrapper<UserPet> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("pet_id", petId);
        
        boolean success = this.remove(queryWrapper);
        if (!success) {
            log.warn("用户宠物关联不存在或删除失败: userId={}, petId={}", userId, petId);
            return;
        }

        log.info("移除用户宠物关联成功: userId={}, petId={}", userId, petId);
    }

    @Override
    public List<Integer> getPetIdsByUserId(Integer userId) {
        if (userId == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        
        return this.baseMapper.getPetIdsByUserId(userId);
    }

    @Override
    public List<Integer> getUserIdsByPetId(Integer petId) {
        if (petId == null) {
            throw new RuntimeException("宠物ID不能为空");
        }
        
        return this.baseMapper.getUserIdsByPetId(petId);
    }

    @Override
    public boolean isUserPetOwner(Integer userId, Integer petId) {
        if (userId == null || petId == null) {
            return false;
        }
        
        return this.baseMapper.isUserPetOwner(userId, petId);
    }

    @Override
    @Transactional
    public void addUserPetRelations(Integer userId, List<Integer> petIds) {
        if (userId == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        if (petIds == null || petIds.isEmpty()) {
            return;
        }

        // 批量创建关联
        for (Integer petId : petIds) {
            if (petId != null) {
                try {
                    addUserPetRelation(userId, petId);
                } catch (Exception e) {
                    log.warn("添加用户宠物关联失败: userId={}, petId={}, error={}", userId, petId, e.getMessage());
                }
            }
        }

        log.info("批量添加用户宠物关联完成: userId={}, petCount={}", userId, petIds.size());
    }

    @Override
    @Transactional
    public void removeAllUserPetRelations(Integer userId) {
        if (userId == null) {
            throw new RuntimeException("用户ID不能为空");
        }

        QueryWrapper<UserPet> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        
        boolean success = this.remove(queryWrapper);
        if (success) {
            log.info("移除用户所有宠物关联成功: userId={}", userId);
        } else {
            log.warn("用户没有宠物关联或移除失败: userId={}", userId);
        }
    }

    @Override
    @Transactional
    public void removeAllPetUserRelations(Integer petId) {
        if (petId == null) {
            throw new RuntimeException("宠物ID不能为空");
        }

        QueryWrapper<UserPet> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("pet_id", petId);
        
        boolean success = this.remove(queryWrapper);
        if (success) {
            log.info("移除宠物所有用户关联成功: petId={}", petId);
        } else {
            log.warn("宠物没有用户关联或移除失败: petId={}", petId);
        }
    }
}
