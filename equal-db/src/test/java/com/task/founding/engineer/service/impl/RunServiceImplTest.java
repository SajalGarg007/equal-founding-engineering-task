package com.task.founding.engineer.service.impl;

import com.task.founding.engineer.dto.request.CreateRunRequestDTO;
import com.task.founding.engineer.enums.RunStatus;
import com.task.founding.engineer.model.XRayRun;
import com.task.founding.engineer.repository.XRayRunRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RunServiceImplTest {

    @Mock
    private XRayRunRepository runRepository;

    @InjectMocks
    private RunServiceImpl runService;

    private UUID runId;
    private CreateRunRequestDTO createRequest;
    private XRayRun mockRun;

    @BeforeEach
    void setUp() {
        runId = UUID.randomUUID();

        Map<String, Object> input = new HashMap<>();
        input.put("source", "test-source");
        input.put("batchSize", 100);

        createRequest = CreateRunRequestDTO.builder()
            .pipelineType("data-processing")
            .pipelineId("pipeline-123")
            .input(input)
            .build();

        mockRun = XRayRun.builder()
            .runId(runId)
            .pipelineType("data-processing")
            .pipelineId("pipeline-123")
            .startedAt(LocalDateTime.now())
            .status(RunStatus.IN_PROGRESS)
            .input(input)
            .build();
    }

    @Test
    void testCreateRun_Success() {
        XRayRun savedRun = XRayRun.builder()
            .runId(runId)
            .pipelineType(createRequest.getPipelineType())
            .pipelineId(createRequest.getPipelineId())
            .status(RunStatus.IN_PROGRESS)
            .input(createRequest.getInput())
            .build();

        when(runRepository.save(any(XRayRun.class))).thenReturn(savedRun);

        UUID result = runService.createRun(createRequest);

        assertNotNull(result);
        assertEquals(runId, result);
        
        ArgumentCaptor<XRayRun> runCaptor = ArgumentCaptor.forClass(XRayRun.class);
        verify(runRepository, times(1)).save(runCaptor.capture());
        
        XRayRun capturedRun = runCaptor.getValue();
        assertEquals(createRequest.getPipelineType(), capturedRun.getPipelineType());
        assertEquals(createRequest.getPipelineId(), capturedRun.getPipelineId());
        assertEquals(RunStatus.IN_PROGRESS, capturedRun.getStatus());
        assertEquals(createRequest.getInput(), capturedRun.getInput());
        // Note: startedAt is set by @PrePersist callback in the entity, not by the service
    }

    @Test
    void testGetRunById_Success() {
        when(runRepository.findByIdWithStepsAndCandidates(runId)).thenReturn(Optional.of(mockRun));

        XRayRun result = runService.getRunById(runId);

        assertNotNull(result);
        assertEquals(runId, result.getRunId());
        verify(runRepository, times(1)).findByIdWithStepsAndCandidates(runId);
    }

    @Test
    void testGetRunById_NotFound() {
        when(runRepository.findByIdWithStepsAndCandidates(runId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            runService.getRunById(runId);
        });

        assertTrue(exception.getMessage().contains("Run not found"));
        verify(runRepository, times(1)).findByIdWithStepsAndCandidates(runId);
    }

    @Test
    void testGetAllRuns_NoFilters() {
        List<XRayRun> runs = Collections.singletonList(mockRun);
        when(runRepository.findAll()).thenReturn(runs);

        List<XRayRun> result = runService.getAllRuns(null, null, null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(runRepository, times(1)).findAll();
    }

    @Test
    void testGetAllRuns_WithPipelineType() {
        List<XRayRun> runs = Collections.singletonList(mockRun);
        when(runRepository.findByPipelineType("data-processing")).thenReturn(runs);

        List<XRayRun> result = runService.getAllRuns("data-processing", null, null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(runRepository, times(1)).findByPipelineType("data-processing");
    }

    @Test
    void testGetAllRuns_WithStatus() {
        List<XRayRun> runs = Collections.singletonList(mockRun);
        when(runRepository.findByPipelineTypeAndStatus("data-processing", RunStatus.IN_PROGRESS))
            .thenReturn(runs);

        List<XRayRun> result = runService.getAllRuns("data-processing", RunStatus.IN_PROGRESS, null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(runRepository, times(1)).findByPipelineTypeAndStatus("data-processing", RunStatus.IN_PROGRESS);
    }

    @Test
    void testGetAllRuns_WithDateRange() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        List<XRayRun> runs = Collections.singletonList(mockRun);
        
        when(runRepository.findByStartedAtBetween(startDate, endDate)).thenReturn(runs);

        List<XRayRun> result = runService.getAllRuns(null, null, startDate, endDate);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(runRepository, times(1)).findByStartedAtBetween(startDate, endDate);
    }

    @Test
    void testGetAllRuns_WithPipelineTypeAndDateRange() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        List<XRayRun> runs = Collections.singletonList(mockRun);
        
        when(runRepository.findByPipelineTypeAndStartedAtBetween("data-processing", startDate, endDate))
            .thenReturn(runs);

        List<XRayRun> result = runService.getAllRuns("data-processing", null, startDate, endDate);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(runRepository, times(1))
            .findByPipelineTypeAndStartedAtBetween("data-processing", startDate, endDate);
    }

    @Test
    void testGetAllRuns_WithStartDateOnly() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        List<XRayRun> runs = Collections.singletonList(mockRun);
        
        when(runRepository.findByStartedAtBetween(eq(startDate), any(LocalDateTime.class)))
            .thenReturn(runs);

        List<XRayRun> result = runService.getAllRuns(null, null, startDate, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(runRepository, times(1)).findByStartedAtBetween(eq(startDate), any(LocalDateTime.class));
    }

    @Test
    void testGetAllRuns_WithEndDateOnly() {
        LocalDateTime endDate = LocalDateTime.now();
        List<XRayRun> runs = Collections.singletonList(mockRun);
        
        when(runRepository.findByStartedAtBetween(any(LocalDateTime.class), eq(endDate)))
            .thenReturn(runs);

        List<XRayRun> result = runService.getAllRuns(null, null, null, endDate);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(runRepository, times(1)).findByStartedAtBetween(any(LocalDateTime.class), eq(endDate));
    }

    @Test
    void testCompleteRun_Success() {
        when(runRepository.findById(runId)).thenReturn(Optional.of(mockRun));
        when(runRepository.save(any(XRayRun.class))).thenReturn(mockRun);

        Map<String, Object> output = Collections.singletonMap("result", "success");
        runService.completeRun(runId, output);

        ArgumentCaptor<XRayRun> runCaptor = ArgumentCaptor.forClass(XRayRun.class);
        verify(runRepository, times(1)).save(runCaptor.capture());
        
        XRayRun capturedRun = runCaptor.getValue();
        assertEquals(RunStatus.COMPLETED, capturedRun.getStatus());
        assertNotNull(capturedRun.getCompletedAt());
        assertEquals(output, capturedRun.getOutput());
    }

    @Test
    void testCompleteRun_NotFound() {
        when(runRepository.findById(runId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            runService.completeRun(runId, Collections.singletonMap("result", "success"));
        });

        assertTrue(exception.getMessage().contains("Run not found"));
        verify(runRepository, never()).save(any());
    }

    @Test
    void testFailRun_Success() {
        when(runRepository.findById(runId)).thenReturn(Optional.of(mockRun));
        when(runRepository.save(any(XRayRun.class))).thenReturn(mockRun);

        runService.failRun(runId);

        ArgumentCaptor<XRayRun> runCaptor = ArgumentCaptor.forClass(XRayRun.class);
        verify(runRepository, times(1)).save(runCaptor.capture());
        
        XRayRun capturedRun = runCaptor.getValue();
        assertEquals(RunStatus.FAILED, capturedRun.getStatus());
        assertNotNull(capturedRun.getCompletedAt());
    }

    @Test
    void testFailRun_NotFound() {
        when(runRepository.findById(runId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            runService.failRun(runId);
        });

        assertTrue(exception.getMessage().contains("Run not found"));
        verify(runRepository, never()).save(any());
    }

    @Test
    void testGetAllRuns_EmptyList() {
        when(runRepository.findAll()).thenReturn(Collections.emptyList());

        List<XRayRun> result = runService.getAllRuns(null, null, null, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}

