package com.task.founding.engineer.service;

import com.task.founding.engineer.dto.request.CreateStepRequestDTO;
import com.task.founding.engineer.model.XRayStep;

import java.util.List;
import java.util.UUID;

public interface StepService {

    UUID createStep(UUID runId, CreateStepRequestDTO request);

    XRayStep getStepById(UUID stepId);

    List<XRayStep> getStepsByRunId(UUID runId);

    List<XRayStep> getStepsByType(String stepType);

    void completeStep(UUID stepId, Object output, String reasoning);
}
