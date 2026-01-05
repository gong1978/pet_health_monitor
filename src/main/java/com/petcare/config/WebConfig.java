package com.petcare.config;

import com.petcare.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置类
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
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
                        "/api/files/upload", // 核心：排除文件上传接口，防止拦截
                        "/uploads/**");      // 核心：排除静态资源访问，防止照片不显示
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        /**
         * 关键修复：将 /uploads/** 同时映射到两个物理存放路径
         * 1. 宠物管理照片: F:/.../uploads/pets/
         * 2. 行为识别照片: F:/.../uploads/pet_behavior/
         */
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(
                        "file:F:/软件工程/软件工程大作业/智能宠物健康与行为监测系统/uploads/pets/",
                        "file:F:/软件工程/软件工程大作业/智能宠物健康与行为监测系统/uploads/pet_behavior/"
                );
    }
}