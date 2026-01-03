package com.task.founding.engineer.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "xray_candidates", indexes = {
    @Index(name = "idx_candidate_step_id", columnList = "step_id"),
    @Index(name = "idx_candidate_selected", columnList = "selected"),
    @Index(name = "idx_candidate_score", columnList = "score")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XRayCandidate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "candidate_id")
    private UUID candidateId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id", nullable = false)
    private XRayStep step;

    @Column(name = "step_id", insertable = false, updatable = false)
    private UUID stepId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data", nullable = false, columnDefinition = "jsonb")
    private Object data;

    @Column(name = "score")
    private Double score;

    @Column(name = "selected", nullable = false)
    @Builder.Default
    private Boolean selected = false;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Object metadata;
}

