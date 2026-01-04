package com.task.founding.engineer.sdk.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartRunRequest {

    private String pipelineType;

    private String pipelineId;

    private Object input;
}

