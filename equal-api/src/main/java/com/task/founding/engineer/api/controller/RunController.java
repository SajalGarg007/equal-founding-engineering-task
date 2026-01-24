package com.task.founding.engineer.api.controller;

import com.task.founding.engineer.api.controller.converter.RunConverter;
import com.task.founding.engineer.dto.request.CreateRunRequestDTO;
import com.task.founding.engineer.dto.response.ApiResponse;
import com.task.founding.engineer.dto.response.IdResponseDTO;
import com.task.founding.engineer.dto.response.RunResponseDTO;
import com.task.founding.engineer.enums.RunStatus;
import com.task.founding.engineer.service.RunService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/runs")
@RequiredArgsConstructor
public class RunController {

    private final RunService runService;
    private final RunConverter runConverter;

    @PostMapping
    public ResponseEntity<ApiResponse<IdResponseDTO>> createRun(@Valid @RequestBody CreateRunRequestDTO request) {
        UUID runId = runService.createRun(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Run created successfully", IdResponseDTO.of(runId)));
    }

    @GetMapping("/{runId}")
    public ResponseEntity<ApiResponse<RunResponseDTO>> getRun(@PathVariable UUID runId) {
        RunResponseDTO run = runConverter.toResponse(runService.getRunById(runId));
        return ResponseEntity.ok(ApiResponse.success(run));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RunResponseDTO>>> getAllRuns(
            @RequestParam(required = false) String pipelineType,
            @RequestParam(required = false) RunStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<RunResponseDTO> runs = runService.getAllRuns(
                        pipelineType, status, startDate, endDate).stream()
                .map(runConverter::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(runs));
    }

    @PutMapping("/{runId}/complete")
    public ResponseEntity<ApiResponse<Void>> completeRun(
            @PathVariable UUID runId,
            @RequestBody(required = false) Object output) {
        runService.completeRun(runId, output);
        return ResponseEntity.ok(ApiResponse.success("Run completed successfully", null));
    }

    @PutMapping("/{runId}/fail")
    public ResponseEntity<ApiResponse<Void>> failRun(@PathVariable UUID runId) {
        runService.failRun(runId);
        return ResponseEntity.ok(ApiResponse.success("Run marked as failed", null));
    }
}

