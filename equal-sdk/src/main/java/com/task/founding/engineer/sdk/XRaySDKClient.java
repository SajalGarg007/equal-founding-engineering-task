package com.task.founding.engineer.sdk;

import com.task.founding.engineer.sdk.annotation.SdkInternalApi;
import com.task.founding.engineer.sdk.client.XRayClient;
import com.task.founding.engineer.sdk.context.XRayContext;
import com.task.founding.engineer.sdk.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
public class XRaySDKClient implements XRaySDK {

    private final XRayClient client;
    private final boolean enableAsync;

    /**
     * Create a builder for constructing XRaySDKClient instances.
     *
     * @return a new builder instance
     */
    public static XRaySDKClientBuilder builder() {
        return XRaySDKClientBuilder.standard();
    }

    @Override
    public StartRunResult startRun(StartRunRequest request) {
        request = beforeClientExecution(request);
        return executeStartRun(request);
    }

    @SdkInternalApi
    final StartRunResult executeStartRun(StartRunRequest startRunRequest) {
        try {
            CompletableFuture<UUID> future = client.createRunAsync(
                    startRunRequest.getPipelineType(),
                    startRunRequest.getPipelineId(),
                    startRunRequest.getInput()
            );

            if (enableAsync) {
                future.thenAccept(runId -> {
                    if (Objects.nonNull(runId)) {
                        XRayContext context = XRayContext.getOrCreate();
                        context.setCurrentRunId(runId);
                        log.debug("Started X-Ray run: {}", runId);
                    }
                });
                // Return immediately with a placeholder result
                return StartRunResult.builder().build();
            } else {
                UUID runId = future.get();
                if (Objects.nonNull(runId)) {
                    XRayContext context = XRayContext.getOrCreate();
                    context.setCurrentRunId(runId);
                    log.debug("Started X-Ray run: {}", runId);
                }
                return StartRunResult.builder().runId(runId).build();
            }
        } catch (Exception e) {
            log.error("Failed to start run", e);
            return StartRunResult.builder().build();
        }
    }

    @Override
    public StartStepResult startStep(StartStepRequest request) {
        request = beforeClientExecution(request);
        return executeStartStep(request);
    }

    @SdkInternalApi
    final StartStepResult executeStartStep(StartStepRequest startStepRequest) {
        XRayContext context = XRayContext.get();
        if (Objects.isNull(context) || Objects.isNull(context.getCurrentRunId())) {
            log.warn("No active run found. Call startRun() first.");
            return StartStepResult.builder().build();
        }

        try {
            CompletableFuture<UUID> future = client.createStepAsync(
                    context.getCurrentRunId(),
                    startStepRequest.getStepName(),
                    startStepRequest.getStepType(),
                    startStepRequest.getOrder(),
                    startStepRequest.getInput(),
                    startStepRequest.getOutput(),
                    startStepRequest.getReasoning(),
                    startStepRequest.getMetadata()
            );

            if (enableAsync) {
                future.thenAccept(stepId -> {
                    if (Objects.nonNull(stepId)) {
                        context.setCurrentStepId(stepId);
                        log.debug("Started X-Ray step: {}", stepId);
                    }
                });
                return StartStepResult.builder().build();
            } else {
                UUID stepId = future.get();
                if (Objects.nonNull(stepId)) {
                    context.setCurrentStepId(stepId);
                    log.debug("Started X-Ray step: {}", stepId);
                }
                return StartStepResult.builder().stepId(stepId).build();
            }
        } catch (Exception e) {
            log.error("Failed to start step", e);
            return StartStepResult.builder().build();
        }
    }

    @Override
    public AddCandidateResult addCandidate(AddCandidateRequest request) {
        request = beforeClientExecution(request);
        return executeAddCandidate(request);
    }

    @SdkInternalApi
    final AddCandidateResult executeAddCandidate(AddCandidateRequest addCandidateRequest) {
        XRayContext context = XRayContext.get();
        if (Objects.isNull(context) || Objects.isNull(context.getCurrentStepId())) {
            log.warn("No active step found. Call startStep() first.");
            return AddCandidateResult.builder().build();
        }

        try {
            CompletableFuture<UUID> future = client.addCandidateAsync(
                    context.getCurrentStepId(),
                    addCandidateRequest.getCandidateData(),
                    addCandidateRequest.getScore(),
                    addCandidateRequest.getSelected(),
                    addCandidateRequest.getRejectionReason(),
                    addCandidateRequest.getMetadata()
            );

            if (enableAsync) {
                future.thenAccept(candidateId -> {
                    if (Objects.nonNull(candidateId)) {
                        log.debug("Added candidate: {}", candidateId);
                    }
                });
                return AddCandidateResult.builder().build();
            } else {
                UUID candidateId = future.get();
                if (Objects.nonNull(candidateId)) {
                    log.debug("Added candidate: {}", candidateId);
                }
                return AddCandidateResult.builder().candidateId(candidateId).build();
            }
        } catch (Exception e) {
            log.error("Failed to add candidate", e);
            return AddCandidateResult.builder().build();
        }
    }

    @Override
    public SelectCandidateResult selectCandidate(SelectCandidateRequest request) {
        request = beforeClientExecution(request);
        return executeSelectCandidate(request);
    }

    @SdkInternalApi
    final SelectCandidateResult executeSelectCandidate(SelectCandidateRequest selectCandidateRequest) {
        // Note: In the current implementation, selection is done via addCandidate with selected=true
        // This method is kept for API consistency and future enhancements
        log.debug("Selecting candidate {} with reasoning: {}", 
                selectCandidateRequest.getCandidateId(), 
                selectCandidateRequest.getReasoning());
        return SelectCandidateResult.builder().build();
    }

    @Override
    public RejectCandidateResult rejectCandidate(RejectCandidateRequest request) {
        request = beforeClientExecution(request);
        return executeRejectCandidate(request);
    }

    @SdkInternalApi
    final RejectCandidateResult executeRejectCandidate(RejectCandidateRequest rejectCandidateRequest) {
        // Note: In the current implementation, rejection is done via addCandidate with selected=false
        // This method is kept for API consistency and future enhancements
        log.debug("Rejecting candidate {} with reason: {}", 
                rejectCandidateRequest.getCandidateId(), 
                rejectCandidateRequest.getReason());
        return RejectCandidateResult.builder().build();
    }

    @Override
    public EndStepResult endStep(EndStepRequest request) {
        request = beforeClientExecution(request);
        return executeEndStep(request);
    }

    @SdkInternalApi
    final EndStepResult executeEndStep(EndStepRequest endStepRequest) {
        XRayContext context = XRayContext.get();
        if (Objects.isNull(context) || Objects.isNull(context.getCurrentStepId())) {
            log.warn("No active step found. Call startStep() first.");
            return EndStepResult.builder().build();
        }

        try {
            CompletableFuture<Void> future = client.completeStepAsync(
                    context.getCurrentStepId(),
                    endStepRequest.getOutput(),
                    endStepRequest.getReasoning()
            );

            if (enableAsync) {
                future.thenAccept(v -> {
                    log.debug("Completed step: {}", context.getCurrentStepId());
                    context.setCurrentStepId(null);
                });
            } else {
                future.get();
                log.debug("Completed step: {}", context.getCurrentStepId());
                context.setCurrentStepId(null);
            }
            return EndStepResult.builder().build();
        } catch (Exception e) {
            log.error("Failed to end step", e);
            return EndStepResult.builder().build();
        }
    }

    @Override
    public EndRunResult endRun(EndRunRequest request) {
        request = beforeClientExecution(request);
        return executeEndRun(request);
    }

    @SdkInternalApi
    final EndRunResult executeEndRun(EndRunRequest endRunRequest) {
        XRayContext context = XRayContext.get();
        if (Objects.isNull(context) || Objects.isNull(context.getCurrentRunId())) {
            log.warn("No active run found. Call startRun() first.");
            return EndRunResult.builder().build();
        }

        try {
            CompletableFuture<Void> future = client.completeRunAsync(
                    context.getCurrentRunId(), 
                    endRunRequest.getOutput()
            );

            if (enableAsync) {
                future.thenAccept(v -> {
                    log.debug("Completed run: {}", context.getCurrentRunId());
                    XRayContext.clear();
                });
            } else {
                future.get();
                log.debug("Completed run: {}", context.getCurrentRunId());
                XRayContext.clear();
            }
            return EndRunResult.builder().build();
        } catch (Exception e) {
            log.error("Failed to end run", e);
            return EndRunResult.builder().build();
        }
    }

    @Override
    public FailRunResult failRun(FailRunRequest request) {
        request = beforeClientExecution(request);
        return executeFailRun(request);
    }

    @SdkInternalApi
    final FailRunResult executeFailRun(FailRunRequest failRunRequest) {
        XRayContext context = XRayContext.get();
        if (Objects.isNull(context) || Objects.isNull(context.getCurrentRunId())) {
            log.warn("No active run found. Call startRun() first.");
            return FailRunResult.builder().build();
        }

        try {
            CompletableFuture<Void> future = client.failRunAsync(context.getCurrentRunId());

            if (enableAsync) {
                future.thenAccept(v -> {
                    log.debug("Failed run: {}", context.getCurrentRunId());
                    XRayContext.clear();
                });
            } else {
                future.get();
                log.debug("Failed run: {}", context.getCurrentRunId());
                XRayContext.clear();
            }
            return FailRunResult.builder().build();
        } catch (Exception e) {
            log.error("Failed to fail run", e);
            return FailRunResult.builder().build();
        }
    }

    @Override
    public AddMetadataResult addMetadata(AddMetadataRequest request) {
        request = beforeClientExecution(request);
        return executeAddMetadata(request);
    }

    @SdkInternalApi
    final AddMetadataResult executeAddMetadata(AddMetadataRequest addMetadataRequest) {
        XRayContext context = XRayContext.getOrCreate();
        context.getMetadata().put(addMetadataRequest.getKey(), addMetadataRequest.getValue());
        return AddMetadataResult.builder().build();
    }

    @Override
    public UUID getCurrentRunId() {
        XRayContext context = XRayContext.get();
        return Objects.nonNull(context) ? context.getCurrentRunId() : null;
    }

    @Override
    public UUID getCurrentStepId() {
        XRayContext context = XRayContext.get();
        return Objects.nonNull(context) ? context.getCurrentStepId() : null;
    }

    /**
     * Hook called before client execution. Can be overridden for custom processing.
     *
     * @param request the request object
     * @param <T> the request type
     * @return the request (possibly modified)
     */
    protected <T> T beforeClientExecution(T request) {
        return request;
    }
}

