package com.task.founding.engineer.service.impl;

import com.task.founding.engineer.dto.request.CreateRunRequestDTO;
import com.task.founding.engineer.enums.RunStatus;
import com.task.founding.engineer.model.XRayRun;
import com.task.founding.engineer.repository.XRayRunRepository;
import com.task.founding.engineer.service.RunService;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RunServiceImpl implements RunService {

    private final XRayRunRepository runRepository;

    @Override
    @Transactional
    public UUID createRun(@NotNull CreateRunRequestDTO request) {
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
    public XRayRun getRunById(@NotNull UUID runId) {
        return runRepository.findByIdWithStepsAndCandidates(runId)
                .orElseThrow(() -> new RuntimeException("Run not found with id: " + runId));
    }

    @Override
    public List<XRayRun> getAllRuns(
            @Nullable String pipelineType,
            @Nullable RunStatus status,
            @Nullable LocalDateTime startDate,
            @Nullable LocalDateTime endDate) {

        if (Objects.nonNull(startDate) || Objects.nonNull(endDate)) {
            LocalDateTime start = Objects.nonNull(startDate) ? startDate : LocalDateTime.MIN;
            LocalDateTime end = Objects.nonNull(endDate) ? endDate : LocalDateTime.MAX;

            if (Objects.nonNull(pipelineType)) {
                return runRepository.findByPipelineTypeAndStartedAtBetween(pipelineType, start, end);
            } else {
                return runRepository.findByStartedAtBetween(start, end);
            }
        }

        if (Objects.nonNull(pipelineType) && Objects.nonNull(status)) {
            return runRepository.findByPipelineTypeAndStatus(pipelineType, status);
        } else if (Objects.nonNull(pipelineType)) {
            return runRepository.findByPipelineType(pipelineType);
        } else {
            return runRepository.findAll();
        }
    }

    @Override
    @Transactional
    public void completeRun(
            @NotNull UUID runId,
            @NotNull Object output) {
        XRayRun run = runRepository.findById(runId)
                .orElseThrow(() -> new RuntimeException("Run not found with id: " + runId));

        run.setStatus(RunStatus.COMPLETED);
        run.setCompletedAt(LocalDateTime.now());
        run.setOutput(output);
        runRepository.save(run);
    }

    @Override
    @Transactional
    public void failRun(@NotNull UUID runId) {
        XRayRun run = runRepository.findById(runId)
                .orElseThrow(() -> new RuntimeException("Run not found with id: " + runId));

        run.setStatus(RunStatus.FAILED);
        run.setCompletedAt(LocalDateTime.now());
        runRepository.save(run);
    }
}

