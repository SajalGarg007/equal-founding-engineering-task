package com.task.founding.engineer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateResponseDTO {

    private UUID candidateId;
    private UUID stepId;
    private Map<String, Object> data;
    private Double score;
    private Boolean selected;
    private String rejectionReason;
    private Map<String, Object> metadata;
}

