package com.task.founding.engineer.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.task.founding.engineer.api.controller.converter.RunConverter;
import com.task.founding.engineer.dto.request.CreateRunRequestDTO;
import com.task.founding.engineer.dto.response.RunResponseDTO;
import com.task.founding.engineer.enums.RunStatus;
import com.task.founding.engineer.model.XRayRun;
import com.task.founding.engineer.service.RunService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RunControllerTest {

    private MockMvc mockMvc;
    private RunService runService;
    private RunConverter runConverter;
    private ObjectMapper objectMapper;

    private UUID runId;
    private CreateRunRequestDTO createRequest;
    private XRayRun mockRun;
    private RunResponseDTO runResponseDTO;

    @BeforeEach
    void setUp() {
        runService = mock(RunService.class);
        runConverter = mock(RunConverter.class);
        objectMapper = new ObjectMapper();

        RunController controller = new RunController(runService, runConverter);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        runId = UUID.randomUUID();

        // Setup create request
        Map<String, Object> input = new HashMap<>();
        input.put("source", "test-source");
        input.put("batchSize", 100);
        
        createRequest = CreateRunRequestDTO.builder()
            .pipelineType("data-processing")
            .pipelineId("pipeline-123")
            .input(input)
            .build();

        // Setup mock run entity
        mockRun = XRayRun.builder()
            .runId(runId)
            .pipelineType("data-processing")
            .pipelineId("pipeline-123")
            .startedAt(LocalDateTime.now())
            .completedAt(null)
            .status(RunStatus.IN_PROGRESS)
            .input(input)
            .output(null)
            .build();

        // Setup response DTO
        runResponseDTO = RunResponseDTO.builder()
            .runId(runId)
            .pipelineType("data-processing")
            .pipelineId("pipeline-123")
            .startedAt(mockRun.getStartedAt())
            .completedAt(null)
            .status(RunStatus.IN_PROGRESS)
            .input(input)
            .output(null)
            .steps(Collections.emptyList())
            .build();
    }

    @Test
    void testCreateRun_Success() throws Exception {
        when(runService.createRun(any(CreateRunRequestDTO.class)))
            .thenReturn(runId);

        mockMvc.perform(post("/api/v1/runs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Run created successfully"))
            .andExpect(jsonPath("$.data.id").value(runId.toString()));
    }

    @Test
    void testCreateRun_ValidationError_MissingPipelineType() throws Exception {
        CreateRunRequestDTO invalidRequest = CreateRunRequestDTO.builder()
            .pipelineType(null) // Required field is null
            .pipelineId("pipeline-123")
            .input(Collections.singletonMap("key", "value"))
            .build();

        mockMvc.perform(post("/api/v1/runs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateRun_ValidationError_MissingPipelineId() throws Exception {
        CreateRunRequestDTO invalidRequest = CreateRunRequestDTO.builder()
            .pipelineType("data-processing")
            .pipelineId(null) // Required field is null
            .input(Collections.singletonMap("key", "value"))
            .build();

        mockMvc.perform(post("/api/v1/runs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testGetRun_Success() throws Exception {
        when(runService.getRunById(runId))
            .thenReturn(mockRun);
        when(runConverter.toResponse(any(XRayRun.class)))
            .thenReturn(runResponseDTO);

        mockMvc.perform(get("/api/v1/runs/{runId}", runId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.runId").value(runId.toString()))
            .andExpect(jsonPath("$.data.pipelineType").value("data-processing"))
            .andExpect(jsonPath("$.data.pipelineId").value("pipeline-123"))
            .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));
    }

    @Test
    void testGetAllRuns_NoFilters() throws Exception {
        List<XRayRun> runs = Collections.singletonList(mockRun);
        when(runService.getAllRuns(null, null, null, null))
            .thenReturn(runs);
        when(runConverter.toResponse(any(XRayRun.class)))
            .thenReturn(runResponseDTO);

        mockMvc.perform(get("/api/v1/runs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].runId").value(runId.toString()));
    }

    @Test
    void testGetAllRuns_WithPipelineTypeFilter() throws Exception {
        List<XRayRun> runs = Collections.singletonList(mockRun);
        when(runService.getAllRuns(eq("data-processing"), eq(null), eq(null), eq(null)))
            .thenReturn(runs);
        when(runConverter.toResponse(any(XRayRun.class)))
            .thenReturn(runResponseDTO);

        mockMvc.perform(get("/api/v1/runs")
                .param("pipelineType", "data-processing"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].pipelineType").value("data-processing"));
    }

    @Test
    void testGetAllRuns_WithStatusFilter() throws Exception {
        List<XRayRun> runs = Collections.singletonList(mockRun);
        when(runService.getAllRuns(eq(null), eq(RunStatus.IN_PROGRESS), eq(null), eq(null)))
            .thenReturn(runs);
        when(runConverter.toResponse(any(XRayRun.class)))
            .thenReturn(runResponseDTO);

        mockMvc.perform(get("/api/v1/runs")
                .param("status", "IN_PROGRESS"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].status").value("IN_PROGRESS"));
    }

    @Test
    void testGetAllRuns_WithDateRangeFilter() throws Exception {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        
        List<XRayRun> runs = Collections.singletonList(mockRun);
        when(runService.getAllRuns(eq(null), eq(null), eq(startDate), eq(endDate)))
            .thenReturn(runs);
        when(runConverter.toResponse(any(XRayRun.class)))
            .thenReturn(runResponseDTO);

        mockMvc.perform(get("/api/v1/runs")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void testGetAllRuns_WithAllFilters() throws Exception {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        
        List<XRayRun> runs = Collections.singletonList(mockRun);
        when(runService.getAllRuns(eq("data-processing"), eq(RunStatus.IN_PROGRESS), eq(startDate), eq(endDate)))
            .thenReturn(runs);
        when(runConverter.toResponse(any(XRayRun.class)))
            .thenReturn(runResponseDTO);

        mockMvc.perform(get("/api/v1/runs")
                .param("pipelineType", "data-processing")
                .param("status", "IN_PROGRESS")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void testGetAllRuns_EmptyList() throws Exception {
        when(runService.getAllRuns(null, null, null, null))
            .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/runs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void testCompleteRun_Success() throws Exception {
        Map<String, Object> output = Collections.singletonMap("result", "success");
        
        mockMvc.perform(put("/api/v1/runs/{runId}/complete", runId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(output)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Run completed successfully"));
    }

    @Test
    void testCompleteRun_WithoutOutput() throws Exception {
        mockMvc.perform(put("/api/v1/runs/{runId}/complete", runId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Run completed successfully"));
    }

    @Test
    void testFailRun_Success() throws Exception {
        mockMvc.perform(put("/api/v1/runs/{runId}/fail", runId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Run marked as failed"));
    }

    @Test
    void testGetAllRuns_MultipleRuns() throws Exception {
        UUID runId2 = UUID.randomUUID();
        XRayRun run2 = XRayRun.builder()
            .runId(runId2)
            .pipelineType("data-processing")
            .pipelineId("pipeline-456")
            .startedAt(LocalDateTime.now())
            .status(RunStatus.COMPLETED)
            .input(Collections.singletonMap("key", "value"))
            .build();

        RunResponseDTO runResponseDTO2 = RunResponseDTO.builder()
            .runId(runId2)
            .pipelineType("data-processing")
            .pipelineId("pipeline-456")
            .startedAt(run2.getStartedAt())
            .status(RunStatus.COMPLETED)
            .input(Collections.singletonMap("key", "value"))
            .steps(Collections.emptyList())
            .build();

        List<XRayRun> runs = Arrays.asList(mockRun, run2);
        when(runService.getAllRuns(null, null, null, null))
            .thenReturn(runs);
        when(runConverter.toResponse(mockRun))
            .thenReturn(runResponseDTO);
        when(runConverter.toResponse(run2))
            .thenReturn(runResponseDTO2);

        mockMvc.perform(get("/api/v1/runs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(2))
            .andExpect(jsonPath("$.data[0].runId").value(runId.toString()))
            .andExpect(jsonPath("$.data[1].runId").value(runId2.toString()));
    }
}

