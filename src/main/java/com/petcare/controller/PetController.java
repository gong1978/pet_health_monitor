package com.petcare.controller;

import com.petcare.common.Result;
import com.petcare.dto.PetCreateRequest;
import com.petcare.dto.PetPageResponse;
import com.petcare.dto.PetQueryRequest;
import com.petcare.dto.PetUpdateRequest;
import com.petcare.entity.Pet;
import com.petcare.service.PetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * 宠物控制器
 */
@Slf4j
@RestController
@RequestMapping("/pets")
public class PetController {

    @Autowired
    private PetService petService;

    /**
     * 分页查询宠物
     */
    @GetMapping
    public Result<PetPageResponse> pageQuery(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String species,
            @RequestParam(required = false) String breed,
            @RequestParam(required = false) String gender,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        log.info("分页查询宠物: name={}, species={}, breed={}, gender={}, page={}, size={}", 
                name, species, breed, gender, page, size);
        
        try {
            PetQueryRequest queryRequest = PetQueryRequest.builder()
                    .name(name)
                    .species(species)
                    .breed(breed)
                    .gender(gender)
                    .page(page)
                    .size(size)
                    .build();
            
            PetPageResponse response = petService.pageQuery(queryRequest);
            return Result.success(response);
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 根据ID查询宠物详情
     */
    @GetMapping("/{petId}")
    public Result<Pet> getPetById(@PathVariable Integer petId) {
        log.info("查询宠物详情: {}", petId);
        try {
            Pet pet = petService.getPetById(petId);
            return Result.success(pet);
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 创建宠物
     */
    @PostMapping
    public Result<String> createPet(@RequestBody PetCreateRequest createRequest, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未授权");
        }
        
        log.info("创建宠物: {}, userId={}", createRequest.getName(), userId);
        try {
            petService.createPet(createRequest, userId);
            return Result.success("创建宠物成功");
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 更新宠物信息
     */
    @PutMapping("/{petId}")
    public Result<String> updatePet(@PathVariable Integer petId, 
                                   @RequestBody PetUpdateRequest updateRequest,
                                   HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未授权");
        }
        
        // 设置宠物ID
        updateRequest.setPetId(petId);
        
        log.info("更新宠物信息: {}", petId);
        try {
            petService.updatePet(updateRequest);
            return Result.success("更新宠物信息成功");
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 删除宠物
     */
    @DeleteMapping("/{petId}")
    public Result<String> deletePet(@PathVariable Integer petId, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未授权");
        }
        
        log.info("删除宠物: {}", petId);
        try {
            petService.deletePet(petId);
            return Result.success("删除宠物成功");
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 批量删除宠物
     */
    @DeleteMapping("/batch")
    public Result<String> batchDeletePets(@RequestBody List<Integer> petIds, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未授权");
        }
        
        log.info("批量删除宠物: {}", petIds);
        try {
            petService.batchDeletePets(petIds);
            return Result.success("批量删除宠物成功");
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 上传宠物图片
     */
    @PostMapping("/upload")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未授权");
        }
        
        if (file.isEmpty()) {
            return Result.fail(400, "上传文件不能为空");
        }

        // 验证文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !isValidImageFile(originalFilename)) {
            return Result.fail(400, "只支持 jpg、jpeg、png、gif 格式的图片文件");
        }

        // 验证文件大小（限制为5MB）
        if (file.getSize() > 5 * 1024 * 1024) {
            return Result.fail(400, "文件大小不能超过5MB");
        }

        try {
            // 生成唯一文件名
            String fileExtension = getFileExtension(originalFilename);
            String fileName = UUID.randomUUID().toString() + "." + fileExtension;
            
            // 创建上传目录
            String uploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "pets";
            File uploadDirFile = new File(uploadDir);
            if (!uploadDirFile.exists()) {
                uploadDirFile.mkdirs();
            }
            
            // 保存文件
            String filePath = uploadDir + File.separator + fileName;
            file.transferTo(new File(filePath));
            
            // 返回文件访问URL
            String fileUrl = "/uploads/pets/" + fileName;
            log.info("上传宠物图片成功: {}", fileUrl);
            
            return Result.success(fileUrl);
        } catch (IOException e) {
            log.error("上传文件失败", e);
            return Result.fail(500, "上传文件失败");
        }
    }

    /**
     * 验证是否为有效的图片文件
     */
    private boolean isValidImageFile(String fileName) {
        String lowerCaseName = fileName.toLowerCase();
        return lowerCaseName.endsWith(".jpg") || 
               lowerCaseName.endsWith(".jpeg") || 
               lowerCaseName.endsWith(".png") || 
               lowerCaseName.endsWith(".gif");
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        return (lastDotIndex != -1) ? fileName.substring(lastDotIndex + 1) : "";
    }
}
