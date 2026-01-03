package com.task.founding.engineer.service;

import com.task.founding.engineer.dto.request.CreateCandidateRequestDTO;
import com.task.founding.engineer.model.XRayCandidate;

import java.util.List;
import java.util.UUID;

public interface CandidateService {

    UUID createCandidate(UUID stepId, CreateCandidateRequestDTO request);

    List<UUID> createCandidates(UUID stepId, List<CreateCandidateRequestDTO> requests);

    List<XRayCandidate> getCandidatesByStepId(UUID stepId, Boolean selected);

    List<XRayCandidate> getSelectedCandidates(UUID stepId);

    List<XRayCandidate> getRejectedCandidates(UUID stepId);
}
