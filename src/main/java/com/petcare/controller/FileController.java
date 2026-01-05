package com.petcare.controller;

import com.petcare.common.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileController {

    // 设置上传路径 (请根据你的电脑实际路径修改，或者写在配置文件里)
    // Windows示例: D:/pet_uploads/
    // Mac/Linux示例: /Users/username/pet_uploads/
    private static final String UPLOAD_DIR = "F:/软件工程/软件工程大作业/智能宠物健康与行为监测系统/uploads/pet_behavior";

    @PostMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.fail("上传文件不能为空");
        }

        // 1. 获取原始文件名 (用于仿AI识别)
        String originalFilename = file.getOriginalFilename();

        // 2. 生成唯一存储文件名，防止重名覆盖
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFileName = UUID.randomUUID().toString() + suffix;

        // 3. 确保目录存在
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            // 4. 保存文件
            File dest = new File(dir, newFileName);
            file.transferTo(dest);

            // 5. 构造返回数据
            // 这里的 url 是前端访问图片的路径，需要配合 WebConfig 映射
            String fileUrl = "/uploads/" + newFileName;

            // [核心逻辑] 提取文件名作为“识别结果”
            // 去掉扩展名，例如 "狗狗跑酷.jpg" -> "狗狗跑酷"
            String aiResult = originalFilename.substring(0, originalFilename.lastIndexOf("."));

            // 返回一个对象，包含访问路径和识别结果
            return Result.success(new UploadResponse(fileUrl, aiResult));

        } catch (IOException e) {
            e.printStackTrace();
            return Result.fail("文件上传失败: " + e.getMessage());
        }
    }

    // 内部类用于返回数据
    @lombok.Data
    @lombok.AllArgsConstructor
    static class UploadResponse {
        private String url;
        private String recognizedBehavior; // 识别出的行为
    }
}