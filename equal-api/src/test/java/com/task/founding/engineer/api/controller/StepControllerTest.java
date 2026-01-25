package com.task.founding.engineer.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.task.founding.engineer.api.controller.converter.StepConverter;
import com.task.founding.engineer.dto.request.CreateStepRequestDTO;
import com.task.founding.engineer.dto.response.StepResponseDTO;
import com.task.founding.engineer.enums.StepStatus;
import com.task.founding.engineer.model.XRayStep;
import com.task.founding.engineer.service.StepService;
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

class StepControllerTest {

    private MockMvc mockMvc;
    private StepService stepService;
    private StepConverter stepConverter;
    private ObjectMapper objectMapper;

    private UUID runId;
    private UUID stepId;
    private CreateStepRequestDTO createRequest;
    private XRayStep mockStep;
    private StepResponseDTO stepResponseDTO;

    @BeforeEach
    void setUp() {
        stepService = mock(StepService.class);
        stepConverter = mock(StepConverter.class);
        objectMapper = new ObjectMapper();

        StepController controller = new StepController(stepService, stepConverter);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        runId = UUID.randomUUID();
        stepId = UUID.randomUUID();

        // Setup create request
        Map<String, Object> input = new HashMap<>();
        input.put("source", "test-source");
        input.put("batchSize", 50);
        
        createRequest = CreateStepRequestDTO.builder()
            .stepName("Filter Step")
            .stepType("filter")
            .order(1)
            .input(input)
            .metadata(Collections.singletonMap("version", "1.0"))
            .build();

        // Setup mock step entity
        mockStep = XRayStep.builder()
            .stepId(stepId)
            .runId(runId)
            .stepName("Filter Step")
            .stepType("filter")
            .order(1)
            .startedAt(LocalDateTime.now())
            .completedAt(null)
            .status(StepStatus.SUCCESS)
            .input(input)
            .output(null)
            .reasoning(null)
            .metadata(Collections.singletonMap("version", "1.0"))
            .build();

        // Setup response DTO
        stepResponseDTO = StepResponseDTO.builder()
            .stepId(stepId)
            .runId(runId)
            .stepName("Filter Step")
            .stepType("filter")
            .order(1)
            .startedAt(mockStep.getStartedAt())
            .completedAt(null)
            .status(StepStatus.SUCCESS)
            .input(input)
            .output(null)
            .reasoning(null)
            .metadata(Collections.singletonMap("version", "1.0"))
            .candidates(Collections.emptyList())
            .build();
    }

    @Test
    void testCreateStep_Success() throws Exception {
        when(stepService.createStep(eq(runId), any(CreateStepRequestDTO.class)))
            .thenReturn(stepId);

        mockMvc.perform(post("/api/v1/runs/{runId}/steps", runId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Step created successfully"))
            .andExpect(jsonPath("$.data.id").value(stepId.toString()));
    }

    @Test
    void testCreateStep_ValidationError_MissingStepName() throws Exception {
        CreateStepRequestDTO invalidRequest = CreateStepRequestDTO.builder()
            .stepName(null) // Required field is null
            .stepType("filter")
            .order(1)
            .input(Collections.singletonMap("key", "value"))
            .build();

        mockMvc.perform(post("/api/v1/runs/{runId}/steps", runId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateStep_ValidationError_MissingStepType() throws Exception {
        CreateStepRequestDTO invalidRequest = CreateStepRequestDTO.builder()
            .stepName("Filter Step")
            .stepType(null) // Required field is null
            .order(1)
            .input(Collections.singletonMap("key", "value"))
            .build();

        mockMvc.perform(post("/api/v1/runs/{runId}/steps", runId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateStep_ValidationError_MissingOrder() throws Exception {
        CreateStepRequestDTO invalidRequest = CreateStepRequestDTO.builder()
            .stepName("Filter Step")
            .stepType("filter")
            .order(null) // Required field is null
            .input(Collections.singletonMap("key", "value"))
            .build();

        mockMvc.perform(post("/api/v1/runs/{runId}/steps", runId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testGetStep_Success() throws Exception {
        when(stepService.getStepById(stepId))
            .thenReturn(mockStep);
        when(stepConverter.toResponse(any(XRayStep.class)))
            .thenReturn(stepResponseDTO);

        mockMvc.perform(get("/api/v1/steps/{stepId}", stepId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.stepId").value(stepId.toString()))
            .andExpect(jsonPath("$.data.runId").value(runId.toString()))
            .andExpect(jsonPath("$.data.stepName").value("Filter Step"))
            .andExpect(jsonPath("$.data.stepType").value("filter"))
            .andExpect(jsonPath("$.data.status").value("SUCCESS"));
    }

    @Test
    void testGetStepsByRunId_Success() throws Exception {
        List<XRayStep> steps = Collections.singletonList(mockStep);
        when(stepService.getStepsByRunId(runId))
            .thenReturn(steps);
        when(stepConverter.toResponse(any(XRayStep.class)))
            .thenReturn(stepResponseDTO);

        mockMvc.perform(get("/api/v1/runs/{runId}/steps", runId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].stepId").value(stepId.toString()))
            .andExpect(jsonPath("$.data[0].runId").value(runId.toString()));
    }

    @Test
    void testGetStepsByRunId_EmptyList() throws Exception {
        when(stepService.getStepsByRunId(runId))
            .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/runs/{runId}/steps", runId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void testGetStepsByType_WithStepType() throws Exception {
        List<XRayStep> steps = Collections.singletonList(mockStep);
        when(stepService.getStepsByType("filter"))
            .thenReturn(steps);
        when(stepConverter.toResponse(any(XRayStep.class)))
            .thenReturn(stepResponseDTO);

        mockMvc.perform(get("/api/v1/steps")
                .param("stepType", "filter"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].stepType").value("filter"));
    }

    @Test
    void testGetStepsByType_WithoutStepType() throws Exception {
        mockMvc.perform(get("/api/v1/steps"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void testGetStepsByType_WithDifferentStepType() throws Exception {
        UUID stepId2 = UUID.randomUUID();
        XRayStep step2 = XRayStep.builder()
            .stepId(stepId2)
            .runId(runId)
            .stepName("Generation Step")
            .stepType("generation")
            .order(2)
            .startedAt(LocalDateTime.now())
            .status(StepStatus.SUCCESS)
            .build();

        StepResponseDTO stepResponseDTO2 = StepResponseDTO.builder()
            .stepId(stepId2)
            .runId(runId)
            .stepName("Generation Step")
            .stepType("generation")
            .order(2)
            .startedAt(step2.getStartedAt())
            .status(StepStatus.SUCCESS)
            .candidates(Collections.emptyList())
            .build();

        List<XRayStep> steps = Collections.singletonList(step2);
        when(stepService.getStepsByType("generation"))
            .thenReturn(steps);
        when(stepConverter.toResponse(any(XRayStep.class)))
            .thenReturn(stepResponseDTO2);

        mockMvc.perform(get("/api/v1/steps")
                .param("stepType", "generation"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].stepType").value("generation"));
    }

    @Test
    void testCompleteStep_WithOutputAndReasoning() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("output", Collections.singletonMap("result", "success"));
        body.put("reasoning", "All candidates passed the filter criteria");

        mockMvc.perform(put("/api/v1/steps/{stepId}/complete", stepId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Step completed successfully"));
    }

    @Test
    void testCompleteStep_WithOutputOnly() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("output", Collections.singletonMap("result", "success"));

        mockMvc.perform(put("/api/v1/steps/{stepId}/complete", stepId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Step completed successfully"));
    }

    @Test
    void testCompleteStep_WithoutBody() throws Exception {
        mockMvc.perform(put("/api/v1/steps/{stepId}/complete", stepId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Step completed successfully"));
    }

    @Test
    void testCompleteStep_WithEmptyBody() throws Exception {
        mockMvc.perform(put("/api/v1/steps/{stepId}/complete", stepId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Step completed successfully"));
    }

    @Test
    void testGetStepsByRunId_MultipleSteps() throws Exception {
        UUID stepId2 = UUID.randomUUID();
        XRayStep step2 = XRayStep.builder()
            .stepId(stepId2)
            .runId(runId)
            .stepName("Generation Step")
            .stepType("generation")
            .order(2)
            .startedAt(LocalDateTime.now())
            .status(StepStatus.SUCCESS)
            .build();

        StepResponseDTO stepResponseDTO2 = StepResponseDTO.builder()
            .stepId(stepId2)
            .runId(runId)
            .stepName("Generation Step")
            .stepType("generation")
            .order(2)
            .startedAt(step2.getStartedAt())
            .status(StepStatus.SUCCESS)
            .candidates(Collections.emptyList())
            .build();

        List<XRayStep> steps = Arrays.asList(mockStep, step2);
        when(stepService.getStepsByRunId(runId))
            .thenReturn(steps);
        when(stepConverter.toResponse(mockStep))
            .thenReturn(stepResponseDTO);
        when(stepConverter.toResponse(step2))
            .thenReturn(stepResponseDTO2);

        mockMvc.perform(get("/api/v1/runs/{runId}/steps", runId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(2))
            .andExpect(jsonPath("$.data[0].stepId").value(stepId.toString()))
            .andExpect(jsonPath("$.data[1].stepId").value(stepId2.toString()));
    }
}

