package com.task.founding.engineer.service.impl;

import com.task.founding.engineer.dto.request.CreateStepRequestDTO;
import com.task.founding.engineer.enums.StepStatus;
import com.task.founding.engineer.model.XRayRun;
import com.task.founding.engineer.model.XRayStep;
import com.task.founding.engineer.repository.XRayRunRepository;
import com.task.founding.engineer.repository.XRayStepRepository;
import com.task.founding.engineer.service.CandidateService;
import com.task.founding.engineer.service.StepService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StepServiceImpl implements StepService {

    private final XRayStepRepository stepRepository;
    private final XRayRunRepository runRepository;
    private final CandidateService candidateService;

    @Override
    @Transactional
    public UUID createStep(UUID runId, CreateStepRequestDTO request) {
        XRayRun run = runRepository.findById(runId)
                .orElseThrow(() -> new RuntimeException("Run not found with id: " + runId));

        XRayStep step = XRayStep.builder()
                .run(run)
                .stepName(request.getStepName())
                .stepType(request.getStepType())
                .order(request.getOrder())
                .status(StepStatus.SUCCESS)
                .input(request.getInput())
                .output(request.getOutput())
                .reasoning(request.getReasoning())
                .metadata(request.getMetadata())
                .build();

        step = stepRepository.save(step);

        // Create candidates if provided
        if (request.getCandidates() != null && !request.getCandidates().isEmpty()) {
            candidateService.createCandidates(step.getStepId(), request.getCandidates());
        }

        return step.getStepId();
    }

    @Override
    @Transactional(readOnly = true)
    public XRayStep getStepById(UUID stepId) {
        return stepRepository.findById(stepId)
                .orElseThrow(() -> new RuntimeException("Step not found with id: " + stepId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<XRayStep> getStepsByRunId(UUID runId) {
        return stepRepository.findByRunIdOrderByOrderAsc(runId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<XRayStep> getStepsByType(String stepType) {
        return stepRepository.findByStepType(stepType);
    }

    @Override
    @Transactional
    public void completeStep(UUID stepId, Object output, String reasoning) {
        XRayStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new RuntimeException("Step not found with id: " + stepId));

        step.setCompletedAt(LocalDateTime.now());
        step.setOutput(output);
        if (reasoning != null) {
            step.setReasoning(reasoning);
        }
        stepRepository.save(step);
    }
}

