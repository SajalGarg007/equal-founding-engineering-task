package com.task.founding.engineer.api.controller;

import com.task.founding.engineer.dto.response.FilteringStatsResponseDTO;
import com.task.founding.engineer.service.AnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AnalyticsControllerTest {

    private MockMvc mockMvc;
    private AnalyticsService analyticsService;

    private FilteringStatsResponseDTO mockStats;

    @BeforeEach
    void setUp() {
        analyticsService = mock(AnalyticsService.class);

        AnalyticsController controller = new AnalyticsController(analyticsService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        // Setup mock stats
        mockStats = FilteringStatsResponseDTO.builder()
            .totalFilteringSteps(10L)
            .totalInputCandidates(1000L)
            .totalOutputCandidates(750L)
            .averageRejectionRate(0.25)
            .minRejectionRate(0.10)
            .maxRejectionRate(0.40)
            .build();
    }

    @Test
    void testGetFilteringStats_NoFilters() throws Exception {
        when(analyticsService.getFilteringStats(null, null, null))
            .thenReturn(mockStats);

        mockMvc.perform(get("/api/v1/analytics/filtering_stats"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.totalFilteringSteps").value(10))
            .andExpect(jsonPath("$.data.totalInputCandidates").value(1000))
            .andExpect(jsonPath("$.data.totalOutputCandidates").value(750))
            .andExpect(jsonPath("$.data.averageRejectionRate").value(0.25))
            .andExpect(jsonPath("$.data.minRejectionRate").value(0.10))
            .andExpect(jsonPath("$.data.maxRejectionRate").value(0.40));
    }

    @Test
    void testGetFilteringStats_WithPipelineType() throws Exception {
        when(analyticsService.getFilteringStats(eq("data-processing"), eq(null), eq(null)))
            .thenReturn(mockStats);

        mockMvc.perform(get("/api/v1/analytics/filtering_stats")
                .param("pipelineType", "data-processing"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.totalFilteringSteps").value(10))
            .andExpect(jsonPath("$.data.totalInputCandidates").value(1000))
            .andExpect(jsonPath("$.data.totalOutputCandidates").value(750));
    }

    @Test
    void testGetFilteringStats_WithDateRange() throws Exception {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();

        when(analyticsService.getFilteringStats(eq(null), eq(startDate), eq(endDate)))
            .thenReturn(mockStats);

        mockMvc.perform(get("/api/v1/analytics/filtering_stats")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.totalFilteringSteps").value(10))
            .andExpect(jsonPath("$.data.totalInputCandidates").value(1000))
            .andExpect(jsonPath("$.data.totalOutputCandidates").value(750));
    }

    @Test
    void testGetFilteringStats_WithAllFilters() throws Exception {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();

        when(analyticsService.getFilteringStats(eq("data-processing"), eq(startDate), eq(endDate)))
            .thenReturn(mockStats);

        mockMvc.perform(get("/api/v1/analytics/filtering_stats")
                .param("pipelineType", "data-processing")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.totalFilteringSteps").value(10))
            .andExpect(jsonPath("$.data.totalInputCandidates").value(1000))
            .andExpect(jsonPath("$.data.totalOutputCandidates").value(750));
    }

    @Test
    void testGetFilteringStats_EmptyResults() throws Exception {
        FilteringStatsResponseDTO emptyStats = FilteringStatsResponseDTO.builder()
            .totalFilteringSteps(0L)
            .totalInputCandidates(0L)
            .totalOutputCandidates(0L)
            .averageRejectionRate(0.0)
            .minRejectionRate(0.0)
            .maxRejectionRate(0.0)
            .build();

        when(analyticsService.getFilteringStats(null, null, null))
            .thenReturn(emptyStats);

        mockMvc.perform(get("/api/v1/analytics/filtering_stats"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.totalFilteringSteps").value(0))
            .andExpect(jsonPath("$.data.totalInputCandidates").value(0))
            .andExpect(jsonPath("$.data.totalOutputCandidates").value(0))
            .andExpect(jsonPath("$.data.averageRejectionRate").value(0.0))
            .andExpect(jsonPath("$.data.minRejectionRate").value(0.0))
            .andExpect(jsonPath("$.data.maxRejectionRate").value(0.0));
    }

    @Test
    void testGetFilteringStats_HighRejectionRate() throws Exception {
        FilteringStatsResponseDTO highRejectionStats = FilteringStatsResponseDTO.builder()
            .totalFilteringSteps(5L)
            .totalInputCandidates(1000L)
            .totalOutputCandidates(200L)
            .averageRejectionRate(0.80)
            .minRejectionRate(0.70)
            .maxRejectionRate(0.90)
            .build();

        when(analyticsService.getFilteringStats(null, null, null))
            .thenReturn(highRejectionStats);

        mockMvc.perform(get("/api/v1/analytics/filtering_stats"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.totalFilteringSteps").value(5))
            .andExpect(jsonPath("$.data.totalInputCandidates").value(1000))
            .andExpect(jsonPath("$.data.totalOutputCandidates").value(200))
            .andExpect(jsonPath("$.data.averageRejectionRate").value(0.80))
            .andExpect(jsonPath("$.data.minRejectionRate").value(0.70))
            .andExpect(jsonPath("$.data.maxRejectionRate").value(0.90));
    }

    @Test
    void testGetFilteringStats_LowRejectionRate() throws Exception {
        FilteringStatsResponseDTO lowRejectionStats = FilteringStatsResponseDTO.builder()
            .totalFilteringSteps(3L)
            .totalInputCandidates(1000L)
            .totalOutputCandidates(950L)
            .averageRejectionRate(0.05)
            .minRejectionRate(0.02)
            .maxRejectionRate(0.08)
            .build();

        when(analyticsService.getFilteringStats(null, null, null))
            .thenReturn(lowRejectionStats);

        mockMvc.perform(get("/api/v1/analytics/filtering_stats"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.totalFilteringSteps").value(3))
            .andExpect(jsonPath("$.data.totalInputCandidates").value(1000))
            .andExpect(jsonPath("$.data.totalOutputCandidates").value(950))
            .andExpect(jsonPath("$.data.averageRejectionRate").value(0.05))
            .andExpect(jsonPath("$.data.minRejectionRate").value(0.02))
            .andExpect(jsonPath("$.data.maxRejectionRate").value(0.08));
    }

    @Test
    void testGetFilteringStats_WithStartDateOnly() throws Exception {
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);

        when(analyticsService.getFilteringStats(eq(null), eq(startDate), eq(null)))
            .thenReturn(mockStats);

        mockMvc.perform(get("/api/v1/analytics/filtering_stats")
                .param("startDate", startDate.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.totalFilteringSteps").value(10));
    }

    @Test
    void testGetFilteringStats_WithEndDateOnly() throws Exception {
        LocalDateTime endDate = LocalDateTime.now();

        when(analyticsService.getFilteringStats(eq(null), eq(null), eq(endDate)))
            .thenReturn(mockStats);

        mockMvc.perform(get("/api/v1/analytics/filtering_stats")
                .param("endDate", endDate.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.totalFilteringSteps").value(10));
    }
}

