package com.task.founding.engineer.api.controller;

import com.task.founding.engineer.dto.response.ApiResponse;
import com.task.founding.engineer.dto.response.FilteringStatsResponseDTO;
import com.task.founding.engineer.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/filtering_stats")
    public ResponseEntity<ApiResponse<FilteringStatsResponseDTO>> getFilteringStats(
            @RequestParam(required = false) String pipelineType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        FilteringStatsResponseDTO stats = analyticsService.getFilteringStats(
                pipelineType, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}

