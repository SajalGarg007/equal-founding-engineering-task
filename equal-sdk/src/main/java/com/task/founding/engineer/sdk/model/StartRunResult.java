package com.task.founding.engineer.sdk.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Result of the StartRun operation returned by the service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartRunResult {

    private UUID runId;
}

