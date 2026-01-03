package com.task.founding.engineer.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchCreateCandidatesRequestDTO {

    @NotEmpty(message = "candidates list cannot be empty")
    private List<CreateCandidateRequestDTO> candidates;
}

