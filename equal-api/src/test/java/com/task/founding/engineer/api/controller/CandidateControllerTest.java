package com.task.founding.engineer.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.task.founding.engineer.api.controller.converter.CandidateConverter;
import com.task.founding.engineer.dto.request.BatchCreateCandidatesRequestDTO;
import com.task.founding.engineer.dto.request.CreateCandidateRequestDTO;
import com.task.founding.engineer.dto.response.CandidateResponseDTO;
import com.task.founding.engineer.model.XRayCandidate;
import com.task.founding.engineer.service.CandidateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CandidateControllerTest {

    private MockMvc mockMvc;
    private CandidateService candidateService;
    private CandidateConverter candidateConverter;
    private ObjectMapper objectMapper;

    private UUID stepId;
    private UUID candidateId;
    private CreateCandidateRequestDTO createRequest;
    private XRayCandidate mockCandidate;
    private CandidateResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        candidateService = mock(CandidateService.class);
        candidateConverter = mock(CandidateConverter.class);
        objectMapper = new ObjectMapper();

        CandidateController controller = new CandidateController(candidateService, candidateConverter);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        stepId = UUID.randomUUID();
        candidateId = UUID.randomUUID();

        // Setup create request
        Map<String, Object> data = new HashMap<>();
        data.put("name", "Test Product");
        data.put("price", 29.99);
        data.put("rating", 4.5);
        
        createRequest = CreateCandidateRequestDTO.builder()
            .data(data)
            .score(0.85)
            .selected(true)
            .rejectionReason(null)
            .metadata(Collections.singletonMap("source", "test"))
            .build();

        // Setup mock candidate entity
        mockCandidate = XRayCandidate.builder()
            .candidateId(candidateId)
            .stepId(stepId)
            .data(data)
            .score(0.85)
            .selected(true)
            .rejectionReason(null)
            .metadata(Collections.singletonMap("source", "test"))
            .build();

        // Setup response DTO
        responseDTO = CandidateResponseDTO.builder()
            .candidateId(candidateId)
            .stepId(stepId)
            .data(data)
            .score(0.85)
            .selected(true)
            .rejectionReason(null)
            .metadata(Collections.singletonMap("source", "test"))
            .build();
    }

    @Test
    void testCreateCandidate_Success() throws Exception {
        when(candidateService.createCandidate(eq(stepId), any(CreateCandidateRequestDTO.class)))
            .thenReturn(candidateId);

        mockMvc.perform(post("/api/v1/steps/{stepId}/candidates", stepId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Candidate created successfully"))
            .andExpect(jsonPath("$.data.id").value(candidateId.toString()));
    }

    @Test
    void testCreateCandidate_ValidationError() throws Exception {
        // Create invalid request (missing required data field)
        CreateCandidateRequestDTO invalidRequest = CreateCandidateRequestDTO.builder()
            .data(null) // Required field is null
            .build();

        mockMvc.perform(post("/api/v1/steps/{stepId}/candidates", stepId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateCandidatesBatch_Success() throws Exception {
        Map<String, Object> data2 = new HashMap<>();
        data2.put("name", "Product 2");
        data2.put("price", 19.99);
        
        List<CreateCandidateRequestDTO> candidates = Arrays.asList(
            createRequest,
            CreateCandidateRequestDTO.builder()
                .data(data2)
                .score(0.75)
                .selected(false)
                .rejectionReason("Price too low")
                .build()
        );

        BatchCreateCandidatesRequestDTO batchRequest = BatchCreateCandidatesRequestDTO.builder()
            .candidates(candidates)
            .build();

        List<UUID> candidateIds = Arrays.asList(candidateId, UUID.randomUUID());
        when(candidateService.createCandidates(eq(stepId), any(List.class)))
            .thenReturn(candidateIds);

        mockMvc.perform(post("/api/v1/steps/{stepId}/candidates/batch", stepId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(batchRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Candidates created successfully"))
            .andExpect(jsonPath("$.data.ids").isArray())
            .andExpect(jsonPath("$.data.ids.length()").value(2));
    }

    @Test
    void testCreateCandidatesBatch_EmptyList() throws Exception {
        BatchCreateCandidatesRequestDTO batchRequest = BatchCreateCandidatesRequestDTO.builder()
            .candidates(Collections.emptyList())
            .build();

        mockMvc.perform(post("/api/v1/steps/{stepId}/candidates/batch", stepId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(batchRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testGetCandidates_All() throws Exception {
        List<XRayCandidate> candidates = Collections.singletonList(mockCandidate);
        when(candidateService.getCandidatesByStepId(stepId, null))
            .thenReturn(candidates);
        when(candidateConverter.toResponse(any(XRayCandidate.class)))
            .thenReturn(responseDTO);

        mockMvc.perform(get("/api/v1/steps/{stepId}/candidates", stepId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].candidateId").value(candidateId.toString()))
            .andExpect(jsonPath("$.data[0].stepId").value(stepId.toString()))
            .andExpect(jsonPath("$.data[0].selected").value(true));
    }

    @Test
    void testGetCandidates_SelectedOnly() throws Exception {
        List<XRayCandidate> candidates = Collections.singletonList(mockCandidate);
        when(candidateService.getCandidatesByStepId(stepId, true))
            .thenReturn(candidates);
        when(candidateConverter.toResponse(any(XRayCandidate.class)))
            .thenReturn(responseDTO);

        mockMvc.perform(get("/api/v1/steps/{stepId}/candidates", stepId)
                .param("selected", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].selected").value(true));
    }

    @Test
    void testGetCandidates_RejectedOnly() throws Exception {
        Map<String, Object> rejectedData = Collections.singletonMap("name", "Rejected Product");
        
        XRayCandidate rejectedCandidate = XRayCandidate.builder()
            .candidateId(UUID.randomUUID())
            .stepId(stepId)
            .data(rejectedData)
            .selected(false)
            .rejectionReason("Price out of range")
            .build();

        CandidateResponseDTO rejectedResponse = CandidateResponseDTO.builder()
            .candidateId(rejectedCandidate.getCandidateId())
            .stepId(stepId)
            .data(rejectedData)
            .selected(false)
            .rejectionReason("Price out of range")
            .build();

        List<XRayCandidate> candidates = Collections.singletonList(rejectedCandidate);
        when(candidateService.getCandidatesByStepId(stepId, false))
            .thenReturn(candidates);
        when(candidateConverter.toResponse(any(XRayCandidate.class)))
            .thenReturn(rejectedResponse);

        mockMvc.perform(get("/api/v1/steps/{stepId}/candidates", stepId)
                .param("selected", "false"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].selected").value(false))
            .andExpect(jsonPath("$.data[0].rejectionReason").value("Price out of range"));
    }

    @Test
    void testGetSelectedCandidates() throws Exception {
        List<XRayCandidate> candidates = Collections.singletonList(mockCandidate);
        when(candidateService.getSelectedCandidates(stepId))
            .thenReturn(candidates);
        when(candidateConverter.toResponse(any(XRayCandidate.class)))
            .thenReturn(responseDTO);

        mockMvc.perform(get("/api/v1/steps/{stepId}/candidates/selected", stepId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].selected").value(true));
    }

    @Test
    void testGetRejectedCandidates() throws Exception {
        Map<String, Object> rejectedData = Collections.singletonMap("name", "Rejected Product");
        
        XRayCandidate rejectedCandidate = XRayCandidate.builder()
            .candidateId(UUID.randomUUID())
            .stepId(stepId)
            .data(rejectedData)
            .selected(false)
            .rejectionReason("Does not meet quality criteria")
            .build();

        CandidateResponseDTO rejectedResponse = CandidateResponseDTO.builder()
            .candidateId(rejectedCandidate.getCandidateId())
            .stepId(stepId)
            .data(rejectedData)
            .selected(false)
            .rejectionReason("Does not meet quality criteria")
            .build();

        List<XRayCandidate> candidates = Collections.singletonList(rejectedCandidate);
        when(candidateService.getRejectedCandidates(stepId))
            .thenReturn(candidates);
        when(candidateConverter.toResponse(any(XRayCandidate.class)))
            .thenReturn(rejectedResponse);

        mockMvc.perform(get("/api/v1/steps/{stepId}/candidates/rejected", stepId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].selected").value(false))
            .andExpect(jsonPath("$.data[0].rejectionReason").value("Does not meet quality criteria"));
    }

    @Test
    void testGetCandidates_EmptyList() throws Exception {
        when(candidateService.getCandidatesByStepId(stepId, null))
            .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/steps/{stepId}/candidates", stepId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void testGetSelectedCandidates_EmptyList() throws Exception {
        when(candidateService.getSelectedCandidates(stepId))
            .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/steps/{stepId}/candidates/selected", stepId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void testGetRejectedCandidates_EmptyList() throws Exception {
        when(candidateService.getRejectedCandidates(stepId))
            .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/steps/{stepId}/candidates/rejected", stepId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(0));
    }
}
