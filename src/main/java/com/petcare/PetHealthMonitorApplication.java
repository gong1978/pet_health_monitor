package com.petcare;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 宠物健康监测系统主应用程序
 */
@SpringBootApplication
@MapperScan("com.petcare.mapper")
@EnableScheduling       //开启定时任务支持
public class PetHealthMonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetHealthMonitorApplication.class, args);
    }
}