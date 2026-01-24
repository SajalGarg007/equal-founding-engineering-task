package com.task.founding.engineer.service.impl;

import com.task.founding.engineer.dto.response.FilteringStatsResponseDTO;
import com.task.founding.engineer.model.XRayStep;
import com.task.founding.engineer.repository.XRayCandidateRepository;
import com.task.founding.engineer.repository.XRayStepRepository;
import com.task.founding.engineer.service.AnalyticsService;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final XRayStepRepository stepRepository;
    private final XRayCandidateRepository candidateRepository;

    @Override
    public FilteringStatsResponseDTO getFilteringStats(
            @Nullable String pipelineType,
            @Nullable LocalDateTime startDate,
            @Nullable LocalDateTime endDate) {
        
        // Find all filtering steps matching the criteria
        List<XRayStep> filteringSteps =
                stepRepository.findFilteringSteps(pipelineType, startDate, endDate);

        if (filteringSteps.isEmpty()) {
            return FilteringStatsResponseDTO.builder()
                    .totalFilteringSteps(0L)
                    .totalInputCandidates(0L)
                    .totalOutputCandidates(0L)
                    .averageRejectionRate(0.0)
                    .minRejectionRate(0.0)
                    .maxRejectionRate(0.0)
                    .build();
        }

        long totalInputCandidates = 0;
        long totalOutputCandidates = 0;
        double totalRejectionRate = 0.0;
        double minRejectionRate = Double.MAX_VALUE;
        double maxRejectionRate = 0.0;
        int stepsWithCandidates = 0;

        for (XRayStep step : filteringSteps) {
            UUID stepId = step.getStepId();
            long inputCount = candidateRepository.countByStepId(stepId);
            
            if (inputCount > 0) {
                long outputCount = candidateRepository.countByStepIdAndSelectedTrue(stepId);
                double rejectionRate = (inputCount - outputCount) / (double) inputCount;
                
                totalInputCandidates += inputCount;
                totalOutputCandidates += outputCount;
                totalRejectionRate += rejectionRate;
                minRejectionRate = Math.min(minRejectionRate, rejectionRate);
                maxRejectionRate = Math.max(maxRejectionRate, rejectionRate);
                stepsWithCandidates++;
            }
        }

        double averageRejectionRate = stepsWithCandidates > 0 
                ? totalRejectionRate / stepsWithCandidates 
                : 0.0;

        if (minRejectionRate == Double.MAX_VALUE) {
            minRejectionRate = 0.0;
        }

        return FilteringStatsResponseDTO.builder()
                .totalFilteringSteps((long) filteringSteps.size())
                .totalInputCandidates(totalInputCandidates)
                .totalOutputCandidates(totalOutputCandidates)
                .averageRejectionRate(averageRejectionRate)
                .minRejectionRate(minRejectionRate)
                .maxRejectionRate(maxRejectionRate)
                .build();
    }
}

