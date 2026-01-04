package com.task.founding.engineer.api.controller.converter;

import com.task.founding.engineer.dto.response.StepResponseDTO;
import com.task.founding.engineer.model.XRayStep;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class StepConverter {

    private final CandidateConverter candidateConverter;

    public StepConverter(CandidateConverter candidateConverter) {
        this.candidateConverter = candidateConverter;
    }

    public StepResponseDTO toResponse(XRayStep step) {
        if (Objects.isNull(step)) {
            return null;
        }

        return StepResponseDTO.builder()
                .stepId(step.getStepId())
                .runId(step.getRunId())
                .stepName(step.getStepName())
                .stepType(step.getStepType())
                .order(step.getOrder())
                .startedAt(step.getStartedAt())
                .completedAt(step.getCompletedAt())
                .status(step.getStatus())
                .input((java.util.Map<String, Object>) step.getInput())
                .output((java.util.Map<String, Object>) step.getOutput())
                .reasoning(step.getReasoning())
                .metadata((java.util.Map<String, Object>) step.getMetadata())
                .candidates(Objects.nonNull(step.getCandidates())
                    ? step.getCandidates().stream()
                        .map(candidateConverter::toResponse)
                        .collect(Collectors.toList())
                    : null)
                .build();
    }
}

