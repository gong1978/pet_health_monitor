package com.petcare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BehaviorAnalysisUpdateRequest {
    private Long behaviorId;
    private Integer petId;
    private String behaviorType;
    private String imageUrl; // 新增字段
    private String startTime;
    private String endTime;
    private Float confidence;
}