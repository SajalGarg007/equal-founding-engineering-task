package com.task.founding.engineer.api.controller.converter;

import com.task.founding.engineer.dto.response.RunResponseDTO;
import com.task.founding.engineer.model.XRayRun;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class RunConverter {

    private final StepConverter stepConverter;

    public RunConverter(StepConverter stepConverter) {
        this.stepConverter = stepConverter;
    }

    public RunResponseDTO toResponse(XRayRun run) {
        if (Objects.isNull(run)) {
            return null;
        }

        return RunResponseDTO.builder()
                .runId(run.getRunId())
                .pipelineType(run.getPipelineType())
                .pipelineId(run.getPipelineId())
                .startedAt(run.getStartedAt())
                .completedAt(run.getCompletedAt())
                .status(run.getStatus())
                .input((java.util.Map<String, Object>) run.getInput())
                .output((java.util.Map<String, Object>) run.getOutput())
                .steps(Objects.nonNull(run.getSteps())
                    ? run.getSteps().stream()
                        .map(stepConverter::toResponse)
                        .collect(Collectors.toList())
                    : null)
                .build();
    }
}

