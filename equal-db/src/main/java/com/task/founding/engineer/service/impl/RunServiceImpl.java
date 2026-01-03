package com.task.founding.engineer.service.impl;

import com.task.founding.engineer.dto.request.CreateRunRequestDTO;
import com.task.founding.engineer.enums.RunStatus;
import com.task.founding.engineer.model.XRayRun;
import com.task.founding.engineer.repository.XRayRunRepository;
import com.task.founding.engineer.service.RunService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RunServiceImpl implements RunService {

    private final XRayRunRepository runRepository;

    @Override
    @Transactional
    public UUID createRun(CreateRunRequestDTO request) {
        XRayRun run = XRayRun.builder()
                .pipelineType(request.getPipelineType())
                .pipelineId(request.getPipelineId())
                .status(RunStatus.IN_PROGRESS)
                .input(request.getInput())
                .build();

        run = runRepository.save(run);
        return run.getRunId();
    }

    @Override
    @Transactional(readOnly = true)
    public XRayRun getRunById(UUID runId) {
        return runRepository.findByIdWithStepsAndCandidates(runId)
                .orElseThrow(() -> new RuntimeException("Run not found with id: " + runId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<XRayRun> getAllRuns(String pipelineType, RunStatus status) {
        if (pipelineType != null && status != null) {
            return runRepository.findByPipelineTypeAndStatus(pipelineType, status);
        } else if (pipelineType != null) {
            return runRepository.findByPipelineType(pipelineType);
        } else {
            return runRepository.findAll();
        }
    }

    @Override
    @Transactional
    public void completeRun(UUID runId, Object output) {
        XRayRun run = runRepository.findById(runId)
                .orElseThrow(() -> new RuntimeException("Run not found with id: " + runId));

        run.setStatus(RunStatus.COMPLETED);
        run.setCompletedAt(LocalDateTime.now());
        run.setOutput(output);
        runRepository.save(run);
    }

    @Override
    @Transactional
    public void failRun(UUID runId) {
        XRayRun run = runRepository.findById(runId)
                .orElseThrow(() -> new RuntimeException("Run not found with id: " + runId));

        run.setStatus(RunStatus.FAILED);
        run.setCompletedAt(LocalDateTime.now());
        runRepository.save(run);
    }
}

