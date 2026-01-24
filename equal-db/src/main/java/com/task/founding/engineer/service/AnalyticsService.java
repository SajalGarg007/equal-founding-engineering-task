package com.task.founding.engineer.service;

import com.task.founding.engineer.dto.response.FilteringStatsResponseDTO;
import jakarta.annotation.Nullable;

import java.time.LocalDateTime;

public interface AnalyticsService {
    FilteringStatsResponseDTO getFilteringStats(
            @Nullable String pipelineType,
            @Nullable LocalDateTime startDate,
            @Nullable LocalDateTime endDate);
}

