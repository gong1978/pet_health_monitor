package com.petcare.controller;

import com.petcare.common.Result;
import com.petcare.entity.Pet;
import com.petcare.service.PetService;
import com.petcare.service.UserPetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 用户宠物关联控制器
 */
@Slf4j
@RestController
@RequestMapping("/user-pets")
public class UserPetController {

    @Autowired
    private UserPetService userPetService;

    @Autowired
    private PetService petService;

    /**
     * 获取当前用户的宠物列表
     */
    @GetMapping("/my-pets")
    public Result<List<Pet>> getMyPets(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未授权");
        }

        try {
            List<Integer> petIds = userPetService.getPetIdsByUserId(userId);
            if (petIds.isEmpty()) {
                return Result.success(List.of());
            }

            List<Pet> pets = petService.listByIds(petIds);
            return Result.success(pets);
        } catch (RuntimeException e) {
            log.error("获取用户宠物列表失败: userId={}, error={}", userId, e.getMessage());
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 为用户添加宠物关联
     */
    @PostMapping("/add-relation")
    public Result<String> addUserPetRelation(@RequestBody Map<String, Integer> requestBody, 
                                           HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未授权");
        }

        Integer petId = requestBody.get("petId");
        if (petId == null) {
            return Result.fail(400, "宠物ID不能为空");
        }

        try {
            userPetService.addUserPetRelation(userId, petId);
            return Result.success("添加宠物关联成功");
        } catch (RuntimeException e) {
            log.error("添加用户宠物关联失败: userId={}, petId={}, error={}", userId, petId, e.getMessage());
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 移除用户宠物关联
     */
    @DeleteMapping("/remove-relation")
    public Result<String> removeUserPetRelation(@RequestBody Map<String, Integer> requestBody, 
                                              HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未授权");
        }

        Integer petId = requestBody.get("petId");
        if (petId == null) {
            return Result.fail(400, "宠物ID不能为空");
        }

        try {
            userPetService.removeUserPetRelation(userId, petId);
            return Result.success("移除宠物关联成功");
        } catch (RuntimeException e) {
            log.error("移除用户宠物关联失败: userId={}, petId={}, error={}", userId, petId, e.getMessage());
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 批量为用户添加宠物关联
     */
    @PostMapping("/batch-add-relations")
    public Result<String> addUserPetRelations(@RequestBody Map<String, List<Integer>> requestBody, 
                                            HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未授权");
        }

        List<Integer> petIds = requestBody.get("petIds");
        if (petIds == null || petIds.isEmpty()) {
            return Result.fail(400, "宠物ID列表不能为空");
        }

        try {
            userPetService.addUserPetRelations(userId, petIds);
            return Result.success("批量添加宠物关联成功");
        } catch (RuntimeException e) {
            log.error("批量添加用户宠物关联失败: userId={}, petIds={}, error={}", userId, petIds, e.getMessage());
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 检查用户是否拥有指定宠物（管理员功能）
     */
    @GetMapping("/check-ownership/{userId}/{petId}")
    public Result<Boolean> checkUserPetOwnership(@PathVariable Integer userId, 
                                               @PathVariable Integer petId,
                                               HttpServletRequest request) {
        Integer currentUserId = (Integer) request.getAttribute("userId");
        if (currentUserId == null) {
            return Result.unauthorized("未授权");
        }

        try {
            boolean isOwner = userPetService.isUserPetOwner(userId, petId);
            return Result.success(isOwner);
        } catch (RuntimeException e) {
            log.error("检查用户宠物关系失败: userId={}, petId={}, error={}", userId, petId, e.getMessage());
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 获取宠物的所有主人（管理员功能）
     */
    @GetMapping("/pet-owners/{petId}")
    public Result<List<Integer>> getPetOwners(@PathVariable Integer petId,
                                            HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未授权");
        }

        try {
            List<Integer> ownerIds = userPetService.getUserIdsByPetId(petId);
            return Result.success(ownerIds);
        } catch (RuntimeException e) {
            log.error("获取宠物主人列表失败: petId={}, error={}", petId, e.getMessage());
            return Result.fail(400, e.getMessage());
        }
    }
}
