package com.task.founding.engineer.service.impl;

import com.task.founding.engineer.dto.request.CreateCandidateRequestDTO;
import com.task.founding.engineer.model.XRayCandidate;
import com.task.founding.engineer.model.XRayStep;
import com.task.founding.engineer.repository.XRayCandidateRepository;
import com.task.founding.engineer.repository.XRayStepRepository;
import com.task.founding.engineer.service.CandidateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CandidateServiceImpl implements CandidateService {

    private final XRayCandidateRepository candidateRepository;
    private final XRayStepRepository stepRepository;

    @Override
    @Transactional
    public UUID createCandidate(UUID stepId, CreateCandidateRequestDTO request) {
        XRayStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new RuntimeException("Step not found with id: " + stepId));

        XRayCandidate candidate = XRayCandidate.builder()
                .step(step)
                .data(request.getData())
                .score(request.getScore())
                .selected(request.getSelected() != null ? request.getSelected() : false)
                .rejectionReason(request.getRejectionReason())
                .metadata(request.getMetadata())
                .build();

        candidate = candidateRepository.save(candidate);
        return candidate.getCandidateId();
    }

    @Override
    @Transactional
    public List<UUID> createCandidates(UUID stepId, List<CreateCandidateRequestDTO> requests) {
        XRayStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new RuntimeException("Step not found with id: " + stepId));

        List<XRayCandidate> candidates = requests.stream()
                .map(request -> XRayCandidate.builder()
                        .step(step)
                        .data(request.getData())
                        .score(request.getScore())
                        .selected(request.getSelected() != null ? request.getSelected() : false)
                        .rejectionReason(request.getRejectionReason())
                        .metadata(request.getMetadata())
                        .build())
                .collect(Collectors.toList());

        candidates = candidateRepository.saveAll(candidates);
        return candidates.stream()
                .map(XRayCandidate::getCandidateId)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<XRayCandidate> getCandidatesByStepId(UUID stepId, Boolean selected) {
        if (selected != null) {
            if (selected) {
                return candidateRepository.findByStepIdAndSelectedTrue(stepId);
            } else {
                return candidateRepository.findByStepIdAndSelectedFalse(stepId);
            }
        } else {
            return candidateRepository.findByStepId(stepId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<XRayCandidate> getSelectedCandidates(UUID stepId) {
        return candidateRepository.findByStepIdAndSelectedTrue(stepId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<XRayCandidate> getRejectedCandidates(UUID stepId) {
        return candidateRepository.findByStepIdAndSelectedFalse(stepId);
    }
}

