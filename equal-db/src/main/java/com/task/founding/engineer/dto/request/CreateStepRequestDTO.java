package com.task.founding.engineer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateStepRequestDTO {

    @NotBlank(message = "stepName is required")
    private String stepName;

    @NotBlank(message = "stepType is required")
    private String stepType;

    @NotNull(message = "order is required")
    private Integer order;

    private Map<String, Object> input;

    private Map<String, Object> output;

    private String reasoning;

    private Map<String, Object> metadata;

    private List<CreateCandidateRequestDTO> candidates;
}

