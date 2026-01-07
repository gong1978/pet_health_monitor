package com.petcare.task;

import com.petcare.dto.BehaviorAnalysisCreateRequest;
import com.petcare.dto.SensorDataCreateRequest;
import com.petcare.entity.Pet;
import com.petcare.service.BehaviorAnalysisService;
import com.petcare.service.PetService;
import com.petcare.service.SensorDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 传感器数据与智能行为模拟定时任务
 */
@Slf4j
@Component
public class SensorDataTask {

    @Autowired
    private PetService petService;

    @Autowired
    private SensorDataService sensorDataService;

    @Autowired
    private BehaviorAnalysisService behaviorAnalysisService;

    private final Random random = new Random();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // [核心] 独立计时器：记录每只宠物 ID 下一次应该执行任务的时间戳 (毫秒)
    // Key: PetId, Value: Next Execution Timestamp
    private final Map<Integer, Long> petNextExecutionMap = new ConcurrentHashMap<>();

    // 图片存储的基础路径 (假设在项目根目录下的 uploads 文件夹)
    private final String UPLOAD_DIR = System.getProperty("user.dir") + File.separator + "uploads";

    /**
     * 任务1：生理数据模拟 (保持不变)
     * 每30分钟执行一次：为所有宠物生成模拟心率/体温
     */
    @Scheduled(cron = "0 0 * * * ?")
    // 您的定时规则
    public void generateSensorData() {
        log.info("【生理数据】开始生成模拟心率/体温数据...");
        List<Pet> pets = petService.list();
        if (pets.isEmpty()) return;

        LocalDateTime now = LocalDateTime.now();

        for (Pet pet : pets) {
            // 随机生成数据
            int heartRate = 60 + random.nextInt(71);
            // 偶尔生成一个高温 (例如 39.5 - 40.5) 来测试报警
            float temperature;
            if (random.nextInt(10) > 7) { // 20% 概率生成高温
                temperature = 39.5f + random.nextFloat();
            } else {
                temperature = 37.5f + random.nextFloat() * 1.5f;
            }
            // 保留一位小数
            temperature = (float) (Math.round(temperature * 10)) / 10;

            // 构造请求对象 (DTO)，而不是直接构造实体
            SensorDataCreateRequest request = SensorDataCreateRequest.builder()
                    .petId(pet.getPetId())
                    .heartRate(heartRate)
                    .temperature(temperature)
                    .activity(random.nextInt(2001))
                    .collectedAt(now.format(formatter)) // 转为字符串传入
                    .build();

            try {
                // [核心修改] 调用业务服务方法，而不是直接 save
                // 这样就会执行 checkAndTriggerAlerts 里的判断逻辑了
                sensorDataService.createSensorData(request);
            } catch (Exception e) {
                log.error("模拟数据生成失败: pet={}", pet.getName(), e);
            }
        }
        log.info("【生理数据】完成，已生成 {} 条记录。", pets.size());
    }

    /**
     * 任务2：智能行为监控 (每只动物独立线程/计时)
     * 逻辑：每分钟轮询一次，检查是否有宠物到了“预定时间”
     */
    @Scheduled(fixedDelay = 60000) // 1分钟轮询一次 (相当于总控时钟)
    public void processPetBehaviors() {
        List<Pet> pets = petService.list();
        if (pets.isEmpty()) return;

        long currentTime = System.currentTimeMillis();

        for (Pet pet : pets) {
            // 1. 获取该宠物下次执行时间，如果是新宠物(Map里没有)，则默认为当前时间(立即执行)
            long nextTime = petNextExecutionMap.getOrDefault(pet.getPetId(), currentTime);

            // 2. 判断时间是否到达
            if (currentTime >= nextTime) {
                // 3. 执行：尝试去文件夹找属于这只宠物的图片并分析
                boolean executed = executePetBehaviorAnalysis(pet);

                // 4. 重新计算这只宠物的下一次时间：当前时间 + 随机(20~40分钟)
                // 20分钟 = 1200000ms, 40分钟 = 2400000ms
                long randomDelay = 1200000L + random.nextInt(1200000);
                petNextExecutionMap.put(pet.getPetId(), currentTime + randomDelay);

                if (executed) {
                    log.info("宠物 [{}] 行为抓拍完成，下次抓拍将在 {} 分钟后", pet.getName(), randomDelay / 60000);
                } else {
                    // 如果没找到图片，也重置时间，避免每分钟都报错
                    log.debug("宠物 [{}] 未找到匹配的行为图片，跳过本次生成", pet.getName());
                }
            }
        }
    }

    /**
     * 核心逻辑：扫描文件夹，匹配宠物名字，解析动作
     */
    private boolean executePetBehaviorAnalysis(Pet pet) {
        File folder = new File(UPLOAD_DIR);
        if (!folder.exists() || !folder.isDirectory()) {
            log.warn("上传目录不存在: {}", UPLOAD_DIR);
            return false;
        }

        // 1. 扫描所有文件名以该宠物名字开头的文件
        // 例如宠物名 "汤姆"，匹配 "汤姆睡觉.png", "汤姆吃饭.jpg"
        File[] matchingFiles = folder.listFiles((dir, name) ->
                name.toLowerCase().startsWith(pet.getName().toLowerCase()) && isImageFile(name));

        if (matchingFiles == null || matchingFiles.length == 0) {
            return false; // 该宠物没有对应的照片
        }

        // 2. 随机选取一张图片
        File selectedFile = matchingFiles[random.nextInt(matchingFiles.length)];
        String fileName = selectedFile.getName();

        // 3. 解析动作 (从文件名中移除宠物名字和扩展名)
        // 例子: "汤姆睡觉.png" -> 移除 "汤姆" -> ".png" -> 剩下 "睡觉"
        String behaviorType = parseBehaviorFromFileName(fileName, pet.getName());

        // 4. 构建相对路径 (存入数据库的路径)
        String dbImageUrl = "/uploads/" + fileName;

        // 5. 生成记录
        try {
            BehaviorAnalysisCreateRequest request = BehaviorAnalysisCreateRequest.builder()
                    .petId(pet.getPetId())
                    .behaviorType(behaviorType)
                    .imageUrl(dbImageUrl)
                    .confidence(0.85f + random.nextFloat() * 0.14f) // 随机置信度
                    .startTime(LocalDateTime.now().format(formatter))
                    .build();

            behaviorAnalysisService.createBehaviorAnalysis(request);
            log.info(">> 生成行为记录: 宠物={}, 动作={}, 图片={}", pet.getName(), behaviorType, fileName);
            return true;
        } catch (Exception e) {
            log.error("生成行为记录失败: {}", pet.getName(), e);
            return false;
        }
    }

    /**
     * 辅助方法：从文件名解析动作
     */
    private String parseBehaviorFromFileName(String fileName, String petName) {
        // 去掉扩展名
        String nameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.'));

        // 去掉开头的宠物名
        if (nameWithoutExt.toLowerCase().startsWith(petName.toLowerCase())) {
            String action = nameWithoutExt.substring(petName.length()).trim();
            // 如果文件名就是 "汤姆.png"，截取后为空，则默认为 "日常状态"
            return StringUtils.hasText(action) ? action : "日常状态";
        }
        return "未知行为";
    }

    /**
     * 辅助方法：检查文件扩展名
     */
    private boolean isImageFile(String name) {
        String lowerName = name.toLowerCase();
        return lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")
                || lowerName.endsWith(".png") || lowerName.endsWith(".gif");
    }
}