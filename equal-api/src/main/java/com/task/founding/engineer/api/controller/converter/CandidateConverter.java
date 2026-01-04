package com.task.founding.engineer.api.controller.converter;

import com.task.founding.engineer.dto.response.CandidateResponseDTO;
import com.task.founding.engineer.model.XRayCandidate;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class CandidateConverter {

    public CandidateResponseDTO toResponse(XRayCandidate candidate) {
        if (Objects.isNull(candidate)) {
            return null;
        }

        return CandidateResponseDTO.builder()
                .candidateId(candidate.getCandidateId())
                .stepId(candidate.getStepId())
                .data((java.util.Map<String, Object>) candidate.getData())
                .score(candidate.getScore())
                .selected(candidate.getSelected())
                .rejectionReason(candidate.getRejectionReason())
                .metadata((java.util.Map<String, Object>) candidate.getMetadata())
                .build();
    }
}

