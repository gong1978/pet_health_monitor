package com.petcare.config;

import com.petcare.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web 配置类
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // [核心修复] 使用 allowedOriginPatterns 代替 allowedOrigins，支持携带凭证(Cookie/Token)
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/login",
                        "/auth/register",
                        "/auth/refresh-token",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "files/upload", // 排除上传接口鉴权，防止前端Token处理不当导致401
                        "/uploads/**");      // 排除静态资源
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 动态获取项目根目录
        String projectPath = System.getProperty("user.dir");

        // 兼容 Windows 和 Linux 路径分隔符
        if (!projectPath.endsWith(File.separator)) {
            projectPath += File.separator;
        }

        // 拼接 uploads 目录: 项目根目录/uploads/
        String uploadPath = "file:" + projectPath + "uploads" + File.separator;

        System.out.println("静态资源映射路径: " + uploadPath);

        // 映射规则：URL 中的 /uploads/** 对应本地磁盘 uploads 文件夹
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }
}