package com.task.founding.engineer.dto.response;

import com.task.founding.engineer.enums.RunStatus;
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
public class RunResponseDTO {

    private UUID runId;
    private String pipelineType;
    private String pipelineId;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private RunStatus status;
    private Map<String, Object> input;
    private Map<String, Object> output;
    private List<StepResponseDTO> steps;
}

