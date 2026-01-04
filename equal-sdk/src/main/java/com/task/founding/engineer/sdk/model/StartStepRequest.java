package com.task.founding.engineer.sdk.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartStepRequest {

    private String stepName;

    private String stepType;

    private Integer order;

    private Object input;

    private Object output;

    private String reasoning;

    private Object metadata;
}

