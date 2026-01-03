package com.task.founding.engineer.model;

import com.task.founding.engineer.enums.RunStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "xray_runs", indexes = {
    @Index(name = "idx_pipeline_type", columnList = "pipeline_type"),
    @Index(name = "idx_pipeline_id", columnList = "pipeline_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_started_at", columnList = "started_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XRayRun {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "run_id")
    private UUID runId;

    @Column(name = "pipeline_type", nullable = false, length = 100)
    private String pipelineType;

    @Column(name = "pipeline_id", nullable = false, length = 255)
    private String pipelineId;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private RunStatus status = RunStatus.IN_PROGRESS;

    @Column(name = "input", columnDefinition = "jsonb")
    private JsonNode input;

    @Column(name = "output", columnDefinition = "jsonb")
    private JsonNode output;

    @OneToMany(mappedBy = "run", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("order ASC")
    @Builder.Default
    private List<XRayStep> steps = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
    }
}

