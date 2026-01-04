package com.task.founding.engineer.service;

import com.task.founding.engineer.dto.request.CreateRunRequestDTO;
import com.task.founding.engineer.enums.RunStatus;
import com.task.founding.engineer.model.XRayRun;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public interface RunService {

    UUID createRun(@NotNull CreateRunRequestDTO request);

    XRayRun getRunById(@NotNull UUID runId);

    List<XRayRun> getAllRuns(
            @Nullable String pipelineType,
            @Nullable RunStatus status);

    void completeRun(
            @NotNull UUID runId,
            @NotNull Object output);

    void failRun(@NotNull UUID runId);
}
