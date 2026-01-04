package com.task.founding.engineer.sdk.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddCandidateRequest {

    private Object candidateData;

    private Double score;

    private Boolean selected;

    private String rejectionReason;

    private Object metadata;
}

