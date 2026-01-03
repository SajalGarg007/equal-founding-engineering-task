package com.task.founding.engineer.api.controller;

import com.task.founding.engineer.api.controller.converter.CandidateConverter;
import com.task.founding.engineer.dto.request.BatchCreateCandidatesRequestDTO;
import com.task.founding.engineer.dto.request.CreateCandidateRequestDTO;
import com.task.founding.engineer.dto.response.ApiResponse;
import com.task.founding.engineer.dto.response.CandidateResponseDTO;
import com.task.founding.engineer.dto.response.IdResponseDTO;
import com.task.founding.engineer.service.CandidateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/steps")
@RequiredArgsConstructor
public class CandidateController {

    private final CandidateService candidateService;
    private final CandidateConverter candidateConverter;

    @PostMapping("/{stepId}/candidates")
    public ResponseEntity<ApiResponse<IdResponseDTO>> createCandidate(
            @PathVariable UUID stepId,
            @Valid @RequestBody CreateCandidateRequestDTO request) {
        UUID candidateId = candidateService.createCandidate(stepId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Candidate created successfully", IdResponseDTO.of(candidateId)));
    }

    @PostMapping("/{stepId}/candidates/batch")
    public ResponseEntity<ApiResponse<IdResponseDTO>> createCandidatesBatch(
            @PathVariable UUID stepId,
            @Valid @RequestBody BatchCreateCandidatesRequestDTO request) {
        List<UUID> candidateIds = candidateService.createCandidates(stepId, request.getCandidates());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Candidates created successfully", IdResponseDTO.of(candidateIds)));
    }

    @GetMapping("/{stepId}/candidates")
    public ResponseEntity<ApiResponse<List<CandidateResponseDTO>>> getCandidates(
            @PathVariable UUID stepId,
            @RequestParam(required = false) Boolean selected) {
        List<CandidateResponseDTO> candidates = candidateService.getCandidatesByStepId(stepId, selected).stream()
                .map(candidateConverter::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(candidates));
    }

    @GetMapping("/{stepId}/candidates/selected")
    public ResponseEntity<ApiResponse<List<CandidateResponseDTO>>> getSelectedCandidates(
            @PathVariable UUID stepId) {
        List<CandidateResponseDTO> candidates = candidateService.getSelectedCandidates(stepId).stream()
                .map(candidateConverter::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(candidates));
    }

    @GetMapping("/{stepId}/candidates/rejected")
    public ResponseEntity<ApiResponse<List<CandidateResponseDTO>>> getRejectedCandidates(
            @PathVariable UUID stepId) {
        List<CandidateResponseDTO> candidates = candidateService.getRejectedCandidates(stepId).stream()
                .map(candidateConverter::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(candidates));
    }
}

