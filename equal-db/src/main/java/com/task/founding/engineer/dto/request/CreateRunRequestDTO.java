package com.task.founding.engineer.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRunRequestDTO {

    @NotBlank(message = "pipelineType is required")
    private String pipelineType;

    @NotBlank(message = "pipelineId is required")
    private String pipelineId;

    @NotNull
    private Map<String, Object> input;
}

