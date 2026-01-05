package com.petcare.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 传感器数据实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sensor_data")
public class SensorData {
    /**
     * 采集数据ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long dataId;

    /**
     * 关联宠物ID
     */
    private Integer petId;

    /**
     * 心率（bpm）
     */
    private Integer heartRate;

    /**
     * 体温（℃）
     */
    private Float temperature;

    /**
     * 活动量（步数或活动指数）
     */
    private Integer activity;

    /**
     * 数据采集时间
     */
    private LocalDateTime collectedAt;
}
