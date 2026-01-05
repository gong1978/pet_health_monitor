/*
 Navicat Premium Dump SQL

 Source Server         : mysql8 for windows
 Source Server Type    : MySQL
 Source Server Version : 80039 (8.0.39)
 Source Host           : localhost:3306
 Source Schema         : pet_health_monitor

 Target Server Type    : MySQL
 Target Server Version : 80039 (8.0.39)
 File Encoding         : 65001

 Date: 28/11/2025 18:06:02
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for alerts
-- ----------------------------
DROP TABLE IF EXISTS `alerts`;
CREATE TABLE `alerts`  (
  `alert_id` int NOT NULL AUTO_INCREMENT COMMENT '预警ID（主键）',
  `pet_id` int NOT NULL COMMENT '宠物ID',
  `alert_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '预警类型（高温/低活动等）',
  `alert_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '预警内容',
  `level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '预警等级 warning/critical',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '预警时间',
  `is_resolved` tinyint(1) NULL DEFAULT 0 COMMENT '是否已处理',
  `resolved_by` int NULL DEFAULT NULL COMMENT '处理用户ID（管理员ID）',
  PRIMARY KEY (`alert_id`) USING BTREE,
  INDEX `pet_id`(`pet_id` ASC) USING BTREE,
  INDEX `resolved_by`(`resolved_by` ASC) USING BTREE,
  CONSTRAINT `alerts_ibfk_1` FOREIGN KEY (`pet_id`) REFERENCES `pets` (`pet_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `alerts_ibfk_2` FOREIGN KEY (`resolved_by`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '异常预警记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of alerts
-- ----------------------------
INSERT INTO `alerts` VALUES (1, 1, '低活动', '检测到宠物活动量明显低于日常水平，疑似疲劳或情绪低落。', 'warning', '2025-11-25 09:50:00', 1, 1);
INSERT INTO `alerts` VALUES (2, 2, '心率异常', '心率持续偏高（>130 bpm），可能受到惊吓或环境压力影响。', 'critical', '2025-11-25 10:12:30', 0, NULL);
INSERT INTO `alerts` VALUES (3, 3, '高温', '体温达到 39.1℃，可能出现轻微中暑或剧烈运动后体温偏高。', 'warning', '2025-11-25 09:05:15', 1, 1);
INSERT INTO `alerts` VALUES (4, 4, '低活动', '连续超过 3 小时未检测到明显活动，可能处于深度睡眠或异常静止。', 'warning', '2025-11-27 12:00:20', 0, NULL);
INSERT INTO `alerts` VALUES (5, 5, '心率波动', '心率波动幅度较大，但仍在可接受范围，建议持续观察。', 'warning', '2025-11-27 08:05:40', 1, 1);
INSERT INTO `alerts` VALUES (6, 6, '高温+低活动', '体温略高（38.6℃）且活动量偏低，可能轻微不适。', 'critical', '2025-11-28 14:25:50', 0, NULL);

-- ----------------------------
-- Table structure for behavior_analysis
-- ----------------------------
DROP TABLE IF EXISTS `behavior_analysis`;
CREATE TABLE `behavior_analysis`  (
  `behavior_id` bigint NOT NULL AUTO_INCREMENT COMMENT '行为记录ID（主键）',
  `pet_id` int NOT NULL COMMENT '宠物ID',
  `behavior_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '行为类型，如sleep/eat/walk',
  `start_time` datetime NULL DEFAULT NULL COMMENT '行为开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '行为结束时间',
  `confidence` float NULL DEFAULT NULL COMMENT '模型识别置信度',
  PRIMARY KEY (`behavior_id`) USING BTREE,
  INDEX `pet_id`(`pet_id` ASC) USING BTREE,
  CONSTRAINT `behavior_analysis_ibfk_1` FOREIGN KEY (`pet_id`) REFERENCES `pets` (`pet_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '宠物行为识别结果表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of behavior_analysis
-- ----------------------------
INSERT INTO `behavior_analysis` VALUES (1, 1, 'walk', '2025-02-16 07:20:00', '2025-02-16 07:55:00', 0.93);
INSERT INTO `behavior_analysis` VALUES (2, 2, 'sleep', '2025-02-16 09:10:00', '2025-02-16 10:45:00', 0.97);
INSERT INTO `behavior_analysis` VALUES (3, 3, 'eat', '2025-02-16 08:10:00', '2025-02-16 08:22:00', 0.88);
INSERT INTO `behavior_analysis` VALUES (4, 4, 'walk', '2025-02-16 11:00:00', '2025-02-16 11:25:00', 0.91);
INSERT INTO `behavior_analysis` VALUES (5, 5, 'sleep', '2025-02-16 12:40:00', '2025-02-16 13:55:00', 0.95);
INSERT INTO `behavior_analysis` VALUES (6, 6, 'eat', '2025-02-16 14:10:00', '2025-02-16 14:17:00', 0.89);

-- ----------------------------
-- Table structure for health_reports
-- ----------------------------
DROP TABLE IF EXISTS `health_reports`;
CREATE TABLE `health_reports`  (
  `report_id` int NOT NULL AUTO_INCREMENT COMMENT '健康报告ID（主键）',
  `pet_id` int NOT NULL COMMENT '宠物ID',
  `report_date` date NOT NULL COMMENT '报告生成日期',
  `health_score` int NULL DEFAULT NULL COMMENT '健康评分',
  `summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '健康总结',
  `suggestions` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '健康/饲养建议',
  `reviewed_by` int NULL DEFAULT NULL COMMENT '审核兽医ID（users.user_id，角色=2）',
  PRIMARY KEY (`report_id`) USING BTREE,
  INDEX `pet_id`(`pet_id` ASC) USING BTREE,
  INDEX `reviewed_by`(`reviewed_by` ASC) USING BTREE,
  CONSTRAINT `health_reports_ibfk_1` FOREIGN KEY (`pet_id`) REFERENCES `pets` (`pet_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `health_reports_ibfk_2` FOREIGN KEY (`reviewed_by`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '健康分析与兽医审核报告' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of health_reports
-- ----------------------------
INSERT INTO `health_reports` VALUES (1, 1, '2025-02-16', 88, '1心率与体温均在正常范围内，活动水平稳定，整体健康状况良好。', '建议保持每日适量散步 2 次，每次 20–30 分钟；定期清洁耳朵并每月驱虫。', 2);
INSERT INTO `health_reports` VALUES (2, 2, '2025-02-16', 92, '体重正常，体温稳定，情绪良好，日常活动充足。', '建议保持高蛋白饮食，控制零食摄入；保持猫砂盆清洁，注意脱毛季节的梳理。', 2);
INSERT INTO `health_reports` VALUES (3, 3, '2025-02-16', 85, '户外运动较多，心率偏高但在可接受范围，整体健康良好。', '建议逐步增加心肺耐力训练，注意关节保养，可补充鱼油或氨糖。', 2);
INSERT INTO `health_reports` VALUES (4, 4, '2025-02-16', 93, '整体状态稳定，心率体温正常，活动量适中。', '建议保持室内环境安静，按季节修剪指甲，维持规律体检。', 2);
INSERT INTO `health_reports` VALUES (5, 5, '2025-02-16', 87, '散步后生理数据良好，体重正常，精神状态佳。', '建议维持每日两次运动；注意牙齿护理，可添加洁牙咬胶。', 2);
INSERT INTO `health_reports` VALUES (6, 6, '2025-02-16', 90, '活动量正常，体温心率均处正常范围，无明显异常。', '建议控制饮食避免肥胖，保持适量运动，关注春季寄生虫防护。', 2);

-- ----------------------------
-- Table structure for pets
-- ----------------------------
DROP TABLE IF EXISTS `pets`;
CREATE TABLE `pets`  (
  `pet_id` int NOT NULL AUTO_INCREMENT COMMENT '宠物ID（主键）',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '宠物名字',
  `species` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '物种（狗/猫等）',
  `breed` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '品种',
  `gender` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '性别',
  `age` int NULL DEFAULT NULL COMMENT '年龄（岁）',
  `weight` float NULL DEFAULT NULL COMMENT '体重（kg）',
  `vaccine_record` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '疫苗记录',
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '宠物图片URL',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`pet_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pets
-- ----------------------------
INSERT INTO `pets` VALUES (1, '豆豆', '狗', '柯基', '公', 2, 10.5, '2023-05-10 注射疫苗：狂犬疫苗；2023-05-10 注射疫苗：六联疫苗；2024-05-12 狂犬疫苗年度加强', 'https://b0.bdstatic.com/6dac2980f99b13edcf437d33997a094d.jpeg', '2025-11-28 15:43:08');
INSERT INTO `pets` VALUES (2, '咪咪', '猫', '英短蓝猫', '母', 1, 3.8, '2024-03-01 三联疫苗首针；2024-03-21 三联疫苗第二针；2024-04-10 三联疫苗第三针；2024-04-10 狂犬疫苗首针', 'https://b0.bdstatic.com/6dac2980f99b13edcf437d33997a094d.jpeg', '2025-11-28 15:43:08');
INSERT INTO `pets` VALUES (3, 'Lucky', '狗', '金毛寻回犬', '公', 4, 28.3, '2021-09-15 狂犬疫苗；2022-09-18 狂犬疫苗加强；2023-09-20 狂犬疫苗加强；2023-03-05 八联疫苗', 'https://b0.bdstatic.com/6dac2980f99b13edcf437d33997a094d.jpeg', '2025-11-28 15:43:08');
INSERT INTO `pets` VALUES (4, '花花', '猫', '布偶猫', '母', 3, 4.5, '2022-06-10 三联疫苗；2022-06-10 狂犬疫苗；2023-06-12 三联+狂犬加强；已绝育', 'https://b0.bdstatic.com/6dac2980f99b13edcf437d33997a094d.jpeg', '2025-11-28 15:43:08');
INSERT INTO `pets` VALUES (5, '奶茶', '狗', '泰迪贵宾', '母', 5, 6.2, '2020-04-02 狂犬疫苗；2021-04-05 狂犬疫苗；2022-04-08 狂犬疫苗；2023-04-10 狂犬疫苗；定期体内外驱虫', 'https://b0.bdstatic.com/6dac2980f99b13edcf437d33997a094d.jpeg', '2025-11-28 15:43:08');
INSERT INTO `pets` VALUES (6, '橘子', '猫', '橘猫', '公', 2, 5.1, '2023-02-20 三联疫苗全程；2023-03-25 狂犬疫苗；2024-03-28 狂犬疫苗加强；2024-04 定期体检，未见异常', 'https://b0.bdstatic.com/6dac2980f99b13edcf437d33997a094d.jpeg', '2025-11-28 15:43:08');
INSERT INTO `pets` VALUES (7, '的说法是', '狗', '但发生', '雄性', 12, 12, '大DASDAD', '/uploads/pets/eb882941-102b-4bc8-8511-1bb49e8c3e9f.jpg', '2025-11-28 15:49:51');

-- ----------------------------
-- Table structure for sensor_data
-- ----------------------------
DROP TABLE IF EXISTS `sensor_data`;
CREATE TABLE `sensor_data`  (
  `data_id` bigint NOT NULL AUTO_INCREMENT COMMENT '采集数据ID（主键）',
  `pet_id` int NOT NULL COMMENT '关联宠物ID',
  `heart_rate` int NULL DEFAULT NULL COMMENT '心率（bpm）',
  `temperature` float NULL DEFAULT NULL COMMENT '体温（℃）',
  `activity` int NULL DEFAULT NULL COMMENT '活动量（步数或活动指数）',
  `collected_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '数据采集时间',
  PRIMARY KEY (`data_id`) USING BTREE,
  INDEX `pet_id`(`pet_id` ASC) USING BTREE,
  CONSTRAINT `sensor_data_ibfk_1` FOREIGN KEY (`pet_id`) REFERENCES `pets` (`pet_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '可穿戴设备采集的数据' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sensor_data
-- ----------------------------
INSERT INTO `sensor_data` VALUES (1, 1, 95, 38.6, 1200, '2025-11-16 09:42:11');
INSERT INTO `sensor_data` VALUES (2, 2, 130, 38.2, 300, '2025-11-16 09:42:11');
INSERT INTO `sensor_data` VALUES (3, 3, 145, 39.1, 3500, '2025-11-16 09:42:11');
INSERT INTO `sensor_data` VALUES (4, 4, 110, 38.4, 450, '2025-11-16 09:42:11');
INSERT INTO `sensor_data` VALUES (5, 5, 125, 38.8, 2100, '2025-11-16 09:42:11');
INSERT INTO `sensor_data` VALUES (6, 6, 140, 38.5, 1200, '2025-11-16 09:42:11');

-- ----------------------------
-- Table structure for user_pet
-- ----------------------------
DROP TABLE IF EXISTS `user_pet`;
CREATE TABLE `user_pet`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` int NOT NULL COMMENT '宠物主人用户ID',
  `pet_id` int NOT NULL COMMENT '宠物ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  INDEX `pet_id`(`pet_id` ASC) USING BTREE,
  CONSTRAINT `user_pet_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `user_pet_ibfk_2` FOREIGN KEY (`pet_id`) REFERENCES `pets` (`pet_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户与宠物的关联表，表示某用户拥有或管理某只宠物' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_pet
-- ----------------------------
INSERT INTO `user_pet` VALUES (1, 1, 1);
INSERT INTO `user_pet` VALUES (2, 2, 2);
INSERT INTO `user_pet` VALUES (3, 3, 3);
INSERT INTO `user_pet` VALUES (4, 4, 4);
INSERT INTO `user_pet` VALUES (5, 5, 5);
INSERT INTO `user_pet` VALUES (6, 6, 6);

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `user_id` int NOT NULL AUTO_INCREMENT COMMENT '用户ID（主键）',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '密码（加密存储）',
  `full_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `role` tinyint NOT NULL COMMENT '角色：1管理员、2兽医、3宠物主人',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  INDEX `idx_username`(`username` ASC) USING BTREE,
  INDEX `idx_role`(`role` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统用户表，使用role字段区分不同身份' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'admin', '271ab04e627075e4ba6c6985c142e73d', '超级管理员', 'admin01@petmonitor.com', '13800138000', 1, '2025-11-24 21:10:17');
INSERT INTO `users` VALUES (2, 'tom', '271ab04e627075e4ba6c6985c142e73d', '金莉3', 'jin.vet@petmonitor.com', '13911223344', 2, '2025-11-24 21:10:17');
INSERT INTO `users` VALUES (3, 'jack', '271ab04e627075e4ba6c6985c142e73d', 'Steven Chen', 'steven.vet@petmonitor.com', '13755667788', 2, '2025-11-24 21:10:17');
INSERT INTO `users` VALUES (4, 'lisi', '271ab04e627075e4ba6c6985c142e73d', '李强', 'liqiang@example.com', '13699887766', 3, '2025-11-24 21:10:17');
INSERT INTO `users` VALUES (5, 'wangwu', '271ab04e627075e4ba6c6985c142e73d', 'Mary Liu', 'mary.liu@example.com', '13588776655', 3, '2025-11-24 21:10:17');
INSERT INTO `users` VALUES (6, 'zhaoliu', '271ab04e627075e4ba6c6985c142e73d', '韩雪', 'hanxue@example.com', '15822334455', 3, '2025-11-24 21:10:17');
INSERT INTO `users` VALUES (7, 'mary', '271ab04e627075e4ba6c6985c142e73d', '小甜甜', 'mary@qq.com', '17878787878', 3, '2025-11-24 21:33:20');

-- ----------------------------
-- Table structure for vet_consultations
-- ----------------------------
DROP TABLE IF EXISTS `vet_consultations`;
CREATE TABLE `vet_consultations`  (
  `consult_id` int NOT NULL AUTO_INCREMENT COMMENT '咨询记录ID（主键）',
  `pet_id` int NULL DEFAULT NULL COMMENT '宠物ID',
  `user_id` int NULL DEFAULT NULL COMMENT '提问人（宠物主人ID）',
  `question` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '咨询问题',
  `answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '兽医回复',
  `asked_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提问时间',
  `answered_by` int NULL DEFAULT NULL COMMENT '回答兽医ID（users.user_id，角色=2）',
  PRIMARY KEY (`consult_id`) USING BTREE,
  INDEX `pet_id`(`pet_id` ASC) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  INDEX `answered_by`(`answered_by` ASC) USING BTREE,
  CONSTRAINT `vet_consultations_ibfk_1` FOREIGN KEY (`pet_id`) REFERENCES `pets` (`pet_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `vet_consultations_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `vet_consultations_ibfk_3` FOREIGN KEY (`answered_by`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '宠物健康咨询与兽医回答记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of vet_consultations
-- ----------------------------
INSERT INTO `vet_consultations` VALUES (1, 1, 1, '最近豆豆的活动量比平常少，是不是生病了？', '从当前记录看只是短期低活动，建议观察 1–2 天，并确保正常饮食和睡眠。如出现持续无精打采，建议到院检查。', '2025-02-16 09:58:00', 2);
INSERT INTO `vet_consultations` VALUES (2, 2, 1, '咪咪今天心率有点高，是不是受惊吓了？', '短时心率升高常见于受环境噪音或陌生人影响。建议保持安静环境并观察心率是否恢复。如持续超过 2 小时应进一步检查。', '2025-02-16 10:20:15', 2);
INSERT INTO `vet_consultations` VALUES (3, 3, 1, '带 Lucky 运动后体温有点高，需要特别处理吗？', '运动后体温升高属于正常情况，但若超过 39.5℃ 或伴随呼吸急促，则需立即降温并避免继续剧烈运动。', '2025-02-16 09:10:40', 2);
INSERT INTO `vet_consultations` VALUES (4, 4, 1, '花花今天几乎一整天都在睡觉，这正常吗？', '布偶猫本身性格安静，较长时间睡眠并不一定异常。如果饮食正常且无呕吐精神问题，可以继续观察。', '2025-02-16 12:18:55', 2);
INSERT INTO `vet_consultations` VALUES (5, 5, 1, '奶茶最近心率波动比较大，需要做心脏检查吗？', '轻微波动一般无需担心。若出现持续心率不稳、咳嗽或喘息症状，建议进行心脏超声检查。', '2025-02-16 08:12:32', 2);
INSERT INTO `vet_consultations` VALUES (6, 6, 1, '橘子今天体温偏高一点，还不太爱动，需要去医院吗？', '轻微升温可能是短暂不适。如果超过 38.9℃ 或出现食欲下降，建议带到医院做基础检查。', '2025-02-16 14:30:20', 2);
INSERT INTO `vet_consultations` VALUES (7, 4, 4, '可以打针了不', '满了1一个月就可以了', '2025-11-28 18:04:38', 2);

SET FOREIGN_KEY_CHECKS = 1;
