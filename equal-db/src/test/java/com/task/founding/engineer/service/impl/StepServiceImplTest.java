package com.task.founding.engineer.service.impl;

import com.task.founding.engineer.dto.request.CreateCandidateRequestDTO;
import com.task.founding.engineer.dto.request.CreateStepRequestDTO;
import com.task.founding.engineer.enums.StepStatus;
import com.task.founding.engineer.model.XRayRun;
import com.task.founding.engineer.model.XRayStep;
import com.task.founding.engineer.repository.XRayRunRepository;
import com.task.founding.engineer.repository.XRayStepRepository;
import com.task.founding.engineer.service.CandidateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class StepServiceImplTest {

    @Mock
    private XRayStepRepository stepRepository;

    @Mock
    private XRayRunRepository runRepository;

    @Mock
    private CandidateService candidateService;

    @InjectMocks
    private StepServiceImpl stepService;

    private UUID runId;
    private UUID stepId;
    private XRayRun mockRun;
    private CreateStepRequestDTO createRequest;

    @BeforeEach
    void setUp() {
        runId = UUID.randomUUID();
        stepId = UUID.randomUUID();

        mockRun = XRayRun.builder()
            .runId(runId)
            .pipelineType("data-processing")
            .pipelineId("pipeline-123")
            .build();

        Map<String, Object> input = new HashMap<>();
        input.put("source", "test-source");

        createRequest = CreateStepRequestDTO.builder()
            .stepName("Filter Step")
            .stepType("filter")
            .order(1)
            .input(input)
            .metadata(Collections.singletonMap("version", "1.0"))
            .build();
    }

    @Test
    void testCreateStep_Success() {
        when(runRepository.findById(runId)).thenReturn(Optional.of(mockRun));
        
        XRayStep savedStep = XRayStep.builder()
            .stepId(stepId)
            .run(mockRun)
            .stepName(createRequest.getStepName())
            .stepType(createRequest.getStepType())
            .order(createRequest.getOrder())
            .status(StepStatus.SUCCESS)
            .input(createRequest.getInput())
            .metadata(createRequest.getMetadata())
            .build();
        
        when(stepRepository.save(any(XRayStep.class))).thenReturn(savedStep);

        UUID result = stepService.createStep(runId, createRequest);

        assertNotNull(result);
        assertEquals(stepId, result);
        
        ArgumentCaptor<XRayStep> stepCaptor = ArgumentCaptor.forClass(XRayStep.class);
        verify(stepRepository, times(1)).save(stepCaptor.capture());
        
        XRayStep capturedStep = stepCaptor.getValue();
        assertEquals(createRequest.getStepName(), capturedStep.getStepName());
        assertEquals(createRequest.getStepType(), capturedStep.getStepType());
        assertEquals(StepStatus.SUCCESS, capturedStep.getStatus());
        verify(candidateService, never()).createCandidates(any(), any());
    }

    @Test
    void testCreateStep_WithCandidates() {
        CreateCandidateRequestDTO candidateRequest = CreateCandidateRequestDTO.builder()
            .data(Collections.singletonMap("name", "Test"))
            .score(0.85)
            .selected(true)
            .build();

        createRequest.setCandidates(Collections.singletonList(candidateRequest));

        when(runRepository.findById(runId)).thenReturn(Optional.of(mockRun));
        
        XRayStep savedStep = XRayStep.builder()
            .stepId(stepId)
            .run(mockRun)
            .build();
        
        when(stepRepository.save(any(XRayStep.class))).thenReturn(savedStep);

        UUID result = stepService.createStep(runId, createRequest);

        assertNotNull(result);
        verify(stepRepository, times(1)).save(any(XRayStep.class));
        verify(candidateService, times(1)).createCandidates(eq(stepId), eq(createRequest.getCandidates()));
    }

    @Test
    void testCreateStep_RunNotFound() {
        when(runRepository.findById(runId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            stepService.createStep(runId, createRequest);
        });

        assertTrue(exception.getMessage().contains("Run not found"));
        verify(stepRepository, never()).save(any());
    }

    @Test
    void testGetStepById_Success() {
        XRayStep mockStep = XRayStep.builder()
            .stepId(stepId)
            .runId(runId)
            .stepName("Filter Step")
            .build();

        when(stepRepository.findById(stepId)).thenReturn(Optional.of(mockStep));

        XRayStep result = stepService.getStepById(stepId);

        assertNotNull(result);
        assertEquals(stepId, result.getStepId());
        verify(stepRepository, times(1)).findById(stepId);
    }

    @Test
    void testGetStepById_NotFound() {
        when(stepRepository.findById(stepId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            stepService.getStepById(stepId);
        });

        assertTrue(exception.getMessage().contains("Step not found"));
        verify(stepRepository, times(1)).findById(stepId);
    }

    @Test
    void testGetStepsByRunId() {
        List<XRayStep> steps = Arrays.asList(
            XRayStep.builder().stepId(stepId).runId(runId).order(1).build(),
            XRayStep.builder().stepId(UUID.randomUUID()).runId(runId).order(2).build()
        );

        when(stepRepository.findByRunIdOrderByOrderAsc(runId)).thenReturn(steps);

        List<XRayStep> result = stepService.getStepsByRunId(runId);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(stepRepository, times(1)).findByRunIdOrderByOrderAsc(runId);
    }

    @Test
    void testGetStepsByType() {
        List<XRayStep> steps = Collections.singletonList(
            XRayStep.builder().stepId(stepId).stepType("filter").build()
        );

        when(stepRepository.findByStepType("filter")).thenReturn(steps);

        List<XRayStep> result = stepService.getStepsByType("filter");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(stepRepository, times(1)).findByStepType("filter");
    }

    @Test
    void testCompleteStep_WithReasoning() {
        XRayStep mockStep = XRayStep.builder()
            .stepId(stepId)
            .runId(runId)
            .build();

        when(stepRepository.findById(stepId)).thenReturn(Optional.of(mockStep));
        when(stepRepository.save(any(XRayStep.class))).thenReturn(mockStep);

        Map<String, Object> output = Collections.singletonMap("result", "success");
        String reasoning = "All candidates passed the filter";

        stepService.completeStep(stepId, output, reasoning);

        ArgumentCaptor<XRayStep> stepCaptor = ArgumentCaptor.forClass(XRayStep.class);
        verify(stepRepository, times(1)).save(stepCaptor.capture());
        
        XRayStep capturedStep = stepCaptor.getValue();
        assertNotNull(capturedStep.getCompletedAt());
        assertEquals(output, capturedStep.getOutput());
        assertEquals(reasoning, capturedStep.getReasoning());
    }

    @Test
    void testCompleteStep_WithoutReasoning() {
        XRayStep mockStep = XRayStep.builder()
            .stepId(stepId)
            .runId(runId)
            .build();

        when(stepRepository.findById(stepId)).thenReturn(Optional.of(mockStep));
        when(stepRepository.save(any(XRayStep.class))).thenReturn(mockStep);

        Map<String, Object> output = Collections.singletonMap("result", "success");

        stepService.completeStep(stepId, output, null);

        ArgumentCaptor<XRayStep> stepCaptor = ArgumentCaptor.forClass(XRayStep.class);
        verify(stepRepository, times(1)).save(stepCaptor.capture());
        
        XRayStep capturedStep = stepCaptor.getValue();
        assertNotNull(capturedStep.getCompletedAt());
        assertEquals(output, capturedStep.getOutput());
        assertNull(capturedStep.getReasoning());
    }

    @Test
    void testCompleteStep_NotFound() {
        when(stepRepository.findById(stepId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            stepService.completeStep(stepId, Collections.singletonMap("result", "success"), null);
        });

        assertTrue(exception.getMessage().contains("Step not found"));
        verify(stepRepository, never()).save(any());
    }

    @Test
    void testGetStepsByRunId_EmptyList() {
        when(stepRepository.findByRunIdOrderByOrderAsc(runId)).thenReturn(Collections.emptyList());

        List<XRayStep> result = stepService.getStepsByRunId(runId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testCreateStep_WithEmptyCandidatesList() {
        createRequest.setCandidates(Collections.emptyList());

        when(runRepository.findById(runId)).thenReturn(Optional.of(mockRun));
        
        XRayStep savedStep = XRayStep.builder()
            .stepId(stepId)
            .run(mockRun)
            .build();
        
        when(stepRepository.save(any(XRayStep.class))).thenReturn(savedStep);

        UUID result = stepService.createStep(runId, createRequest);

        assertNotNull(result);
        verify(candidateService, never()).createCandidates(any(), any());
    }
}

