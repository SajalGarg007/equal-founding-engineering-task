package com.task.founding.engineer.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCandidateRequestDTO {

    @NotNull(message = "data is required")
    private Map<String, Object> data;

    private Double score;

    @Builder.Default
    private Boolean selected = false;

    private String rejectionReason;

    private Map<String, Object> metadata;
}

