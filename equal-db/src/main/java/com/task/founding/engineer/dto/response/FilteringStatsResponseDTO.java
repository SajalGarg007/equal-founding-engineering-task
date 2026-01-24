package com.task.founding.engineer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilteringStatsResponseDTO {
    private Long totalFilteringSteps;
    private Long totalInputCandidates;
    private Long totalOutputCandidates;
    private Double averageRejectionRate;
    private Double minRejectionRate;
    private Double maxRejectionRate;
}

