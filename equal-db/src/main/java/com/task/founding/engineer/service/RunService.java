package com.task.founding.engineer.service;

import com.task.founding.engineer.dto.request.CreateRunRequestDTO;
import com.task.founding.engineer.enums.RunStatus;
import com.task.founding.engineer.model.XRayRun;

import java.util.List;
import java.util.UUID;

public interface RunService {

    UUID createRun(CreateRunRequestDTO request);

    XRayRun getRunById(UUID runId);

    List<XRayRun> getAllRuns(String pipelineType, RunStatus status);

    void completeRun(UUID runId, Object output);

    void failRun(UUID runId);
}
