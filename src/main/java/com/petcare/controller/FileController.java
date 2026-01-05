package com.petcare.controller;

import com.petcare.common.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
// [修改点 1] 去掉 /api，因为 application.yml 中的 context-path 已经自动加了 /api
// 现在的完整访问路径变成了： /api + /files = /api/files，与前端一致
@RequestMapping("/files")
public class FileController {

    // [修改点 2] 改为动态路径，避免 F:/ 盘符不存在导致的 500 错误
    private String getBaseUploadDir() {
        // 获取项目运行时的根目录
        String projectPath = System.getProperty("user.dir");
        // 拼接 uploads 目录，自动适配 Windows/Mac/Linux 的分隔符
        return projectPath + File.separator + "uploads" + File.separator;
    }

    @PostMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file,
                         @RequestParam(value = "type", defaultValue = "common") String type) {

        // 打印日志，确认请求已到达
        System.out.println("收到上传请求，类型: " + type);

        if (file.isEmpty()) {
            return Result.fail("文件为空");
        }

        // 1. 确定子文件夹
        String subDir = "common";
        if ("behavior".equals(type)) {
            subDir = "pet_behavior";
        } else if ("pet".equals(type)) {
            subDir = "pets";
        }

        // 2. 获取动态基础路径并创建目录
        String baseDir = getBaseUploadDir();
        File dir = new File(baseDir + subDir);

        // 自动创建目录
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                System.err.println("目录创建失败: " + dir.getAbsolutePath());
                return Result.fail("服务器无法创建上传目录，请检查权限");
            }
        }

        // 3. 生成文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) originalFilename = "unknown.jpg";

        String suffix = "";
        if (originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        } else {
            suffix = ".jpg"; // 默认后缀
        }

        String newFileName = UUID.randomUUID().toString() + suffix;
        File dest = new File(dir, newFileName);

        try {
            // 4. 保存文件
            file.transferTo(dest);
            System.out.println("文件已保存至: " + dest.getAbsolutePath());

            // 5. 返回给前端的 URL (相对路径)
            // 格式： /uploads/pet_behavior/xxxx.jpg
            String url = "/uploads/" + subDir + "/" + newFileName;

            // 简单的“AI”识别结果模拟
            String aiResult = originalFilename;

            return Result.success(new UploadResponse(url, aiResult));
        } catch (IOException e) {
            e.printStackTrace();
            return Result.fail("文件保存异常: " + e.getMessage());
        }
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    static class UploadResponse {
        private String url;
        private String recognizedBehavior;
    }
}