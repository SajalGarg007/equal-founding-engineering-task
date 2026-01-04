package com.task.founding.engineer.api.controller;

import com.task.founding.engineer.api.controller.converter.StepConverter;
import com.task.founding.engineer.dto.request.CreateStepRequestDTO;
import com.task.founding.engineer.dto.response.ApiResponse;
import com.task.founding.engineer.dto.response.IdResponseDTO;
import com.task.founding.engineer.dto.response.StepResponseDTO;
import com.task.founding.engineer.service.StepService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class StepController {

    private final StepService stepService;
    private final StepConverter stepConverter;

    @PostMapping("/runs/{runId}/steps")
    public ResponseEntity<ApiResponse<IdResponseDTO>> createStep(
            @PathVariable UUID runId,
            @Valid @RequestBody CreateStepRequestDTO request) {
        UUID stepId = stepService.createStep(runId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Step created successfully", IdResponseDTO.of(stepId)));
    }

    @GetMapping("/steps/{stepId}")
    public ResponseEntity<ApiResponse<StepResponseDTO>> getStep(@PathVariable UUID stepId) {
        StepResponseDTO step = stepConverter.toResponse(stepService.getStepById(stepId));
        return ResponseEntity.ok(ApiResponse.success(step));
    }

    @GetMapping("/runs/{runId}/steps")
    public ResponseEntity<ApiResponse<List<StepResponseDTO>>> getStepsByRunId(@PathVariable UUID runId) {
        List<StepResponseDTO> steps = stepService.getStepsByRunId(runId).stream()
                .map(stepConverter::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(steps));
    }

    @GetMapping("/steps")
    public ResponseEntity<ApiResponse<List<StepResponseDTO>>> getStepsByType(
            @RequestParam(required = false) String stepType) {
        List<StepResponseDTO> steps;
        if (stepType != null) {
            steps = stepService.getStepsByType(stepType).stream()
                    .map(stepConverter::toResponse)
                    .collect(Collectors.toList());
        } else {
            steps = List.of();
        }
        return ResponseEntity.ok(ApiResponse.success(steps));
    }

    @PutMapping("/steps/{stepId}/complete")
    public ResponseEntity<ApiResponse<Void>> completeStep(
            @PathVariable UUID stepId,
            @RequestBody(required = false) Map<String, Object> body) {
        Object output = body != null ? body.get("output") : null;
        String reasoning = body != null && body.get("reasoning") != null 
            ? body.get("reasoning").toString() 
            : null;
        stepService.completeStep(stepId, output, reasoning);
        return ResponseEntity.ok(ApiResponse.success("Step completed successfully", null));
    }
}

