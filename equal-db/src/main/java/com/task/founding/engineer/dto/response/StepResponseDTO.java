package com.task.founding.engineer.dto.response;

import com.task.founding.engineer.enums.StepStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StepResponseDTO {

    private UUID stepId;
    private UUID runId;
    private String stepName;
    private String stepType;
    private Integer order;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private StepStatus status;
    private Map<String, Object> input;
    private Map<String, Object> output;
    private String reasoning;
    private Map<String, Object> metadata;
    private List<CandidateResponseDTO> candidates;
}

