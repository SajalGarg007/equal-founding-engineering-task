package com.task.founding.engineer.service;

import com.task.founding.engineer.dto.request.CreateCandidateRequestDTO;
import com.task.founding.engineer.model.XRayCandidate;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public interface CandidateService {

    UUID createCandidate(
            @NotNull UUID stepId,
            @NotNull CreateCandidateRequestDTO request);

    List<UUID> createCandidates(
            @NotNull UUID stepId,
            @NotNull List<CreateCandidateRequestDTO> requests);

    List<XRayCandidate> getCandidatesByStepId(
            @NotNull UUID stepId,
            @Nullable Boolean selected);

    List<XRayCandidate> getSelectedCandidates(@NotNull UUID stepId);

    List<XRayCandidate> getRejectedCandidates(@NotNull UUID stepId);
}
