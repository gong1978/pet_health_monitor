package com.petcare.task;

import com.petcare.entity.Pet;
import com.petcare.entity.SensorData;
import com.petcare.service.PetService;
import com.petcare.service.SensorDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

/**
 * 传感器数据定时任务
 */
@Slf4j
@Component
public class SensorDataTask {

    @Autowired
    private PetService petService;

    @Autowired
    private SensorDataService sensorDataService;

    private final Random random = new Random();

    /**
     * 每30分钟执行一次：为所有宠物生成模拟数据
     * cron表达式: 秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0/30 * * * ?")
    public void generateMockData() {
        log.info("开始执行定时任务：生成宠物模拟生理数据...");

        // 1. 获取所有宠物
        List<Pet> pets = petService.list();
        if (pets.isEmpty()) return;

        LocalDateTime now = LocalDateTime.now();

        // 2. 为每只宠物生成数据
        for (Pet pet : pets) {
            // 模拟心率 (60-130 bpm)
            int heartRate = 60 + random.nextInt(71);

            // 模拟体温 (37.5 - 39.5 ℃)
            float temperature = 37.5f + random.nextFloat() * 2.0f;
            // 保留一位小数
            temperature = (float) (Math.round(temperature * 10)) / 10;

            // 模拟活动量 (0-2000步)
            int activity = random.nextInt(2001);

            SensorData data = SensorData.builder()
                    .petId(pet.getPetId())
                    .heartRate(heartRate)
                    .temperature(temperature)
                    .activity(activity)
                    .collectedAt(now)
                    .build();

            sensorDataService.save(data);
        }

        log.info("定时任务结束，已为 {} 只宠物生成数据。", pets.size());
    }
}