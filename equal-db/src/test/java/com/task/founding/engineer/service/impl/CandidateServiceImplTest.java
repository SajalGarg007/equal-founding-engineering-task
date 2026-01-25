package com.task.founding.engineer.service.impl;

import com.task.founding.engineer.dto.request.CreateCandidateRequestDTO;
import com.task.founding.engineer.model.XRayCandidate;
import com.task.founding.engineer.model.XRayStep;
import com.task.founding.engineer.repository.XRayCandidateRepository;
import com.task.founding.engineer.repository.XRayStepRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CandidateServiceImplTest {

    @Mock
    private XRayCandidateRepository candidateRepository;

    @Mock
    private XRayStepRepository stepRepository;

    @InjectMocks
    private CandidateServiceImpl candidateService;

    private UUID stepId;
    private UUID candidateId;
    private XRayStep mockStep;
    private CreateCandidateRequestDTO createRequest;

    @BeforeEach
    void setUp() {
        stepId = UUID.randomUUID();
        candidateId = UUID.randomUUID();

        mockStep = XRayStep.builder()
            .stepId(stepId)
            .stepName("Test Step")
            .stepType("filter")
            .build();

        Map<String, Object> data = new HashMap<>();
        data.put("name", "Test Product");
        data.put("price", 29.99);

        createRequest = CreateCandidateRequestDTO.builder()
            .data(data)
            .score(0.85)
            .selected(true)
            .rejectionReason(null)
            .metadata(Collections.singletonMap("source", "test"))
            .build();
    }

    @Test
    void testCreateCandidate_Success() {
        when(stepRepository.findById(stepId)).thenReturn(Optional.of(mockStep));
        
        XRayCandidate savedCandidate = XRayCandidate.builder()
            .candidateId(candidateId)
            .step(mockStep)
            .data(createRequest.getData())
            .score(createRequest.getScore())
            .selected(true)
            .rejectionReason(null)
            .metadata(createRequest.getMetadata())
            .build();
        
        when(candidateRepository.save(any(XRayCandidate.class))).thenReturn(savedCandidate);

        UUID result = candidateService.createCandidate(stepId, createRequest);

        assertNotNull(result);
        assertEquals(candidateId, result);
        verify(stepRepository, times(1)).findById(stepId);
        verify(candidateRepository, times(1)).save(any(XRayCandidate.class));
    }

    @Test
    void testCreateCandidate_StepNotFound() {
        when(stepRepository.findById(stepId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            candidateService.createCandidate(stepId, createRequest);
        });

        assertTrue(exception.getMessage().contains("Step not found"));
        verify(stepRepository, times(1)).findById(stepId);
        verify(candidateRepository, never()).save(any());
    }

    @Test
    void testCreateCandidate_SelectedNullDefaultsToFalse() {
        CreateCandidateRequestDTO requestWithNullSelected = CreateCandidateRequestDTO.builder()
            .data(Collections.singletonMap("key", "value"))
            .score(0.75)
            .selected(null)
            .build();

        when(stepRepository.findById(stepId)).thenReturn(Optional.of(mockStep));
        
        XRayCandidate savedCandidate = XRayCandidate.builder()
            .candidateId(candidateId)
            .step(mockStep)
            .selected(false)
            .build();
        
        when(candidateRepository.save(any(XRayCandidate.class))).thenReturn(savedCandidate);

        candidateService.createCandidate(stepId, requestWithNullSelected);

        verify(candidateRepository, times(1)).save(argThat(candidate -> 
            !candidate.getSelected()
        ));
    }

    @Test
    void testCreateCandidates_Success() {
        Map<String, Object> data2 = new HashMap<>();
        data2.put("name", "Product 2");
        
        List<CreateCandidateRequestDTO> requests = Arrays.asList(
            createRequest,
            CreateCandidateRequestDTO.builder()
                .data(data2)
                .score(0.75)
                .selected(false)
                .rejectionReason("Price too low")
                .build()
        );

        when(stepRepository.findById(stepId)).thenReturn(Optional.of(mockStep));
        
        UUID candidateId2 = UUID.randomUUID();
        List<XRayCandidate> savedCandidates = Arrays.asList(
            XRayCandidate.builder().candidateId(candidateId).step(mockStep).build(),
            XRayCandidate.builder().candidateId(candidateId2).step(mockStep).build()
        );
        
        when(candidateRepository.saveAll(anyList())).thenReturn(savedCandidates);

        List<UUID> result = candidateService.createCandidates(stepId, requests);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(candidateId, result.get(0));
        assertEquals(candidateId2, result.get(1));
        verify(stepRepository, times(1)).findById(stepId);
        verify(candidateRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testCreateCandidates_StepNotFound() {
        List<CreateCandidateRequestDTO> requests = Collections.singletonList(createRequest);
        when(stepRepository.findById(stepId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            candidateService.createCandidates(stepId, requests);
        });

        assertTrue(exception.getMessage().contains("Step not found"));
        verify(candidateRepository, never()).saveAll(any());
    }

    @Test
    void testGetCandidatesByStepId_AllCandidates() {
        List<XRayCandidate> candidates = Collections.singletonList(
            XRayCandidate.builder().candidateId(candidateId).stepId(stepId).build()
        );
        when(candidateRepository.findByStepId(stepId)).thenReturn(candidates);

        List<XRayCandidate> result = candidateService.getCandidatesByStepId(stepId, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(candidateRepository, times(1)).findByStepId(stepId);
    }

    @Test
    void testGetCandidatesByStepId_SelectedOnly() {
        List<XRayCandidate> candidates = Collections.singletonList(
            XRayCandidate.builder().candidateId(candidateId).stepId(stepId).selected(true).build()
        );
        when(candidateRepository.findByStepIdAndSelectedTrue(stepId)).thenReturn(candidates);

        List<XRayCandidate> result = candidateService.getCandidatesByStepId(stepId, true);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(candidateRepository, times(1)).findByStepIdAndSelectedTrue(stepId);
    }

    @Test
    void testGetCandidatesByStepId_RejectedOnly() {
        List<XRayCandidate> candidates = Collections.singletonList(
            XRayCandidate.builder().candidateId(candidateId).stepId(stepId).selected(false).build()
        );
        when(candidateRepository.findByStepIdAndSelectedFalse(stepId)).thenReturn(candidates);

        List<XRayCandidate> result = candidateService.getCandidatesByStepId(stepId, false);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(candidateRepository, times(1)).findByStepIdAndSelectedFalse(stepId);
    }

    @Test
    void testGetSelectedCandidates() {
        List<XRayCandidate> candidates = Collections.singletonList(
            XRayCandidate.builder().candidateId(candidateId).stepId(stepId).selected(true).build()
        );
        when(candidateRepository.findByStepIdAndSelectedTrue(stepId)).thenReturn(candidates);

        List<XRayCandidate> result = candidateService.getSelectedCandidates(stepId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(candidateRepository, times(1)).findByStepIdAndSelectedTrue(stepId);
    }

    @Test
    void testGetRejectedCandidates() {
        List<XRayCandidate> candidates = Collections.singletonList(
            XRayCandidate.builder().candidateId(candidateId).stepId(stepId).selected(false).build()
        );
        when(candidateRepository.findByStepIdAndSelectedFalse(stepId)).thenReturn(candidates);

        List<XRayCandidate> result = candidateService.getRejectedCandidates(stepId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(candidateRepository, times(1)).findByStepIdAndSelectedFalse(stepId);
    }

    @Test
    void testGetCandidatesByStepId_EmptyList() {
        when(candidateRepository.findByStepId(stepId)).thenReturn(Collections.emptyList());

        List<XRayCandidate> result = candidateService.getCandidatesByStepId(stepId, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}

