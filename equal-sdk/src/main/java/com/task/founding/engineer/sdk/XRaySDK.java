package com.task.founding.engineer.sdk;

import com.task.founding.engineer.sdk.model.*;

import java.util.UUID;

public interface XRaySDK {

    /**
     * Start a new X-Ray run.
     *
     * @param request Container for the parameters to the StartRun operation.
     * @return Result of the StartRun operation returned by the service.
     */
    StartRunResult startRun(StartRunRequest request);

    /**
     * Start a new step within the current run.
     *
     * @param request Container for the parameters to the StartStep operation.
     * @return Result of the StartStep operation returned by the service.
     */
    StartStepResult startStep(StartStepRequest request);

    /**
     * Add a candidate to the current step.
     *
     * @param request Container for the parameters to the AddCandidate operation.
     * @return Result of the AddCandidate operation returned by the service.
     */
    AddCandidateResult addCandidate(AddCandidateRequest request);

    /**
     * Select a candidate (marks it as selected).
     *
     * @param request Container for the parameters to the SelectCandidate operation.
     * @return Result of the SelectCandidate operation returned by the service.
     */
    SelectCandidateResult selectCandidate(SelectCandidateRequest request);

    /**
     * Reject a candidate (marks it as not selected).
     *
     * @param request Container for the parameters to the RejectCandidate operation.
     * @return Result of the RejectCandidate operation returned by the service.
     */
    RejectCandidateResult rejectCandidate(RejectCandidateRequest request);

    /**
     * End the current step.
     *
     * @param request Container for the parameters to the EndStep operation.
     * @return Result of the EndStep operation returned by the service.
     */
    EndStepResult endStep(EndStepRequest request);

    /**
     * End the current run.
     *
     * @param request Container for the parameters to the EndRun operation.
     * @return Result of the EndRun operation returned by the service.
     */
    EndRunResult endRun(EndRunRequest request);

    /**
     * Fail the current run.
     *
     * @param request Container for the parameters to the FailRun operation.
     * @return Result of the FailRun operation returned by the service.
     */
    FailRunResult failRun(FailRunRequest request);

    /**
     * Add metadata to the current context.
     *
     * @param request Container for the parameters to the AddMetadata operation.
     * @return Result of the AddMetadata operation returned by the service.
     */
    AddMetadataResult addMetadata(AddMetadataRequest request);

    /**
     * Get the current run ID from context.
     *
     * @return the current run ID, or null if no active run
     */
    UUID getCurrentRunId();

    /**
     * Get the current step ID from context.
     *
     * @return the current step ID, or null if no active step
     */
    UUID getCurrentStepId();
}
