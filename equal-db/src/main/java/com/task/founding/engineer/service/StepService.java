package com.task.founding.engineer.service;

import com.task.founding.engineer.dto.request.CreateStepRequestDTO;
import com.task.founding.engineer.model.XRayStep;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public interface StepService {

    UUID createStep(@NotNull UUID runId, @NotNull CreateStepRequestDTO request);

    XRayStep getStepById(@NotNull UUID stepId);

    List<XRayStep> getStepsByRunId(@NotNull UUID runId);

    List<XRayStep> getStepsByType(@NotNull String stepType);

    void completeStep(
            @NotNull UUID stepId,
            @NotNull Object output,
            @Nullable String reasoning);
}
