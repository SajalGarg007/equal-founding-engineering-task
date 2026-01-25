package com.task.founding.engineer.service.impl;

import com.task.founding.engineer.dto.response.FilteringStatsResponseDTO;
import com.task.founding.engineer.model.XRayStep;
import com.task.founding.engineer.repository.XRayCandidateRepository;
import com.task.founding.engineer.repository.XRayStepRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceImplTest {

    @Mock
    private XRayStepRepository stepRepository;

    @Mock
    private XRayCandidateRepository candidateRepository;

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    private UUID stepId1;
    private UUID stepId2;
    private XRayStep filteringStep1;
    private XRayStep filteringStep2;

    @BeforeEach
    void setUp() {
        stepId1 = UUID.randomUUID();
        stepId2 = UUID.randomUUID();

        filteringStep1 = XRayStep.builder()
            .stepId(stepId1)
            .stepType("filter")
            .stepName("Filter Step 1")
            .build();

        filteringStep2 = XRayStep.builder()
            .stepId(stepId2)
            .stepType("filter")
            .stepName("Filter Step 2")
            .build();
    }

    @Test
    void testGetFilteringStats_NoFilters() {
        List<XRayStep> steps = Arrays.asList(filteringStep1, filteringStep2);
        when(stepRepository.findFilteringSteps(null, null, null)).thenReturn(steps);
        when(candidateRepository.countByStepId(stepId1)).thenReturn(100L);
        when(candidateRepository.countByStepIdAndSelectedTrue(stepId1)).thenReturn(75L);
        when(candidateRepository.countByStepId(stepId2)).thenReturn(200L);
        when(candidateRepository.countByStepIdAndSelectedTrue(stepId2)).thenReturn(150L);

        FilteringStatsResponseDTO result = analyticsService.getFilteringStats(null, null, null);

        assertNotNull(result);
        assertEquals(2L, result.getTotalFilteringSteps());
        assertEquals(300L, result.getTotalInputCandidates());
        assertEquals(225L, result.getTotalOutputCandidates());
        assertEquals(0.25, result.getAverageRejectionRate(), 0.001);
        assertEquals(0.25, result.getMinRejectionRate(), 0.001);
        assertEquals(0.25, result.getMaxRejectionRate(), 0.001);
    }

    @Test
    void testGetFilteringStats_WithPipelineType() {
        List<XRayStep> steps = Collections.singletonList(filteringStep1);
        when(stepRepository.findFilteringSteps("data-processing", null, null)).thenReturn(steps);
        when(candidateRepository.countByStepId(stepId1)).thenReturn(100L);
        when(candidateRepository.countByStepIdAndSelectedTrue(stepId1)).thenReturn(80L);

        FilteringStatsResponseDTO result = analyticsService.getFilteringStats("data-processing", null, null);

        assertNotNull(result);
        assertEquals(1L, result.getTotalFilteringSteps());
        assertEquals(100L, result.getTotalInputCandidates());
        assertEquals(80L, result.getTotalOutputCandidates());
        assertEquals(0.20, result.getAverageRejectionRate(), 0.001);
    }

    @Test
    void testGetFilteringStats_WithDateRange() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        
        List<XRayStep> steps = Collections.singletonList(filteringStep1);
        when(stepRepository.findFilteringSteps(null, startDate, endDate)).thenReturn(steps);
        when(candidateRepository.countByStepId(stepId1)).thenReturn(150L);
        when(candidateRepository.countByStepIdAndSelectedTrue(stepId1)).thenReturn(100L);

        FilteringStatsResponseDTO result = analyticsService.getFilteringStats(null, startDate, endDate);

        assertNotNull(result);
        assertEquals(1L, result.getTotalFilteringSteps());
        assertEquals(150L, result.getTotalInputCandidates());
        assertEquals(100L, result.getTotalOutputCandidates());
        assertEquals(0.333, result.getAverageRejectionRate(), 0.001);
    }

    @Test
    void testGetFilteringStats_WithAllFilters() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        
        List<XRayStep> steps = Collections.singletonList(filteringStep1);
        when(stepRepository.findFilteringSteps("data-processing", startDate, endDate)).thenReturn(steps);
        when(candidateRepository.countByStepId(stepId1)).thenReturn(200L);
        when(candidateRepository.countByStepIdAndSelectedTrue(stepId1)).thenReturn(120L);

        FilteringStatsResponseDTO result = analyticsService.getFilteringStats("data-processing", startDate, endDate);

        assertNotNull(result);
        assertEquals(1L, result.getTotalFilteringSteps());
        assertEquals(200L, result.getTotalInputCandidates());
        assertEquals(120L, result.getTotalOutputCandidates());
        assertEquals(0.40, result.getAverageRejectionRate(), 0.001);
    }

    @Test
    void testGetFilteringStats_EmptySteps() {
        when(stepRepository.findFilteringSteps(null, null, null)).thenReturn(Collections.emptyList());

        FilteringStatsResponseDTO result = analyticsService.getFilteringStats(null, null, null);

        assertNotNull(result);
        assertEquals(0L, result.getTotalFilteringSteps());
        assertEquals(0L, result.getTotalInputCandidates());
        assertEquals(0L, result.getTotalOutputCandidates());
        assertEquals(0.0, result.getAverageRejectionRate());
        assertEquals(0.0, result.getMinRejectionRate());
        assertEquals(0.0, result.getMaxRejectionRate());
    }

    @Test
    void testGetFilteringStats_StepsWithNoCandidates() {
        List<XRayStep> steps = Arrays.asList(filteringStep1, filteringStep2);
        when(stepRepository.findFilteringSteps(null, null, null)).thenReturn(steps);
        when(candidateRepository.countByStepId(stepId1)).thenReturn(0L);
        when(candidateRepository.countByStepId(stepId2)).thenReturn(0L);

        FilteringStatsResponseDTO result = analyticsService.getFilteringStats(null, null, null);

        assertNotNull(result);
        assertEquals(2L, result.getTotalFilteringSteps());
        assertEquals(0L, result.getTotalInputCandidates());
        assertEquals(0L, result.getTotalOutputCandidates());
        assertEquals(0.0, result.getAverageRejectionRate());
        assertEquals(0.0, result.getMinRejectionRate());
        assertEquals(0.0, result.getMaxRejectionRate());
    }

    @Test
    void testGetFilteringStats_DifferentRejectionRates() {
        List<XRayStep> steps = Arrays.asList(filteringStep1, filteringStep2);
        when(stepRepository.findFilteringSteps(null, null, null)).thenReturn(steps);
        
        // Step 1: 100 input, 90 output = 10% rejection
        when(candidateRepository.countByStepId(stepId1)).thenReturn(100L);
        when(candidateRepository.countByStepIdAndSelectedTrue(stepId1)).thenReturn(90L);
        
        // Step 2: 200 input, 100 output = 50% rejection
        when(candidateRepository.countByStepId(stepId2)).thenReturn(200L);
        when(candidateRepository.countByStepIdAndSelectedTrue(stepId2)).thenReturn(100L);

        FilteringStatsResponseDTO result = analyticsService.getFilteringStats(null, null, null);

        assertNotNull(result);
        assertEquals(2L, result.getTotalFilteringSteps());
        assertEquals(300L, result.getTotalInputCandidates());
        assertEquals(190L, result.getTotalOutputCandidates());
        assertEquals(0.30, result.getAverageRejectionRate(), 0.001); // (0.10 + 0.50) / 2
        assertEquals(0.10, result.getMinRejectionRate(), 0.001);
        assertEquals(0.50, result.getMaxRejectionRate(), 0.001);
    }

    @Test
    void testGetFilteringStats_MixedStepsWithAndWithoutCandidates() {
        List<XRayStep> steps = Arrays.asList(filteringStep1, filteringStep2);
        when(stepRepository.findFilteringSteps(null, null, null)).thenReturn(steps);
        
        // Step 1: Has candidates
        when(candidateRepository.countByStepId(stepId1)).thenReturn(100L);
        when(candidateRepository.countByStepIdAndSelectedTrue(stepId1)).thenReturn(75L);
        
        // Step 2: No candidates
        when(candidateRepository.countByStepId(stepId2)).thenReturn(0L);

        FilteringStatsResponseDTO result = analyticsService.getFilteringStats(null, null, null);

        assertNotNull(result);
        assertEquals(2L, result.getTotalFilteringSteps());
        assertEquals(100L, result.getTotalInputCandidates());
        assertEquals(75L, result.getTotalOutputCandidates());
        assertEquals(0.25, result.getAverageRejectionRate(), 0.001);
        assertEquals(0.25, result.getMinRejectionRate(), 0.001);
        assertEquals(0.25, result.getMaxRejectionRate(), 0.001);
    }

    @Test
    void testGetFilteringStats_AllCandidatesSelected() {
        List<XRayStep> steps = Collections.singletonList(filteringStep1);
        when(stepRepository.findFilteringSteps(null, null, null)).thenReturn(steps);
        when(candidateRepository.countByStepId(stepId1)).thenReturn(100L);
        when(candidateRepository.countByStepIdAndSelectedTrue(stepId1)).thenReturn(100L);

        FilteringStatsResponseDTO result = analyticsService.getFilteringStats(null, null, null);

        assertNotNull(result);
        assertEquals(1L, result.getTotalFilteringSteps());
        assertEquals(100L, result.getTotalInputCandidates());
        assertEquals(100L, result.getTotalOutputCandidates());
        assertEquals(0.0, result.getAverageRejectionRate());
        assertEquals(0.0, result.getMinRejectionRate());
        assertEquals(0.0, result.getMaxRejectionRate());
    }

    @Test
    void testGetFilteringStats_AllCandidatesRejected() {
        List<XRayStep> steps = Collections.singletonList(filteringStep1);
        when(stepRepository.findFilteringSteps(null, null, null)).thenReturn(steps);
        when(candidateRepository.countByStepId(stepId1)).thenReturn(100L);
        when(candidateRepository.countByStepIdAndSelectedTrue(stepId1)).thenReturn(0L);

        FilteringStatsResponseDTO result = analyticsService.getFilteringStats(null, null, null);

        assertNotNull(result);
        assertEquals(1L, result.getTotalFilteringSteps());
        assertEquals(100L, result.getTotalInputCandidates());
        assertEquals(0L, result.getTotalOutputCandidates());
        assertEquals(1.0, result.getAverageRejectionRate());
        assertEquals(1.0, result.getMinRejectionRate());
        assertEquals(1.0, result.getMaxRejectionRate());
    }
}

