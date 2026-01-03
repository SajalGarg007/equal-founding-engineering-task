package com.task.founding.engineer.repository;

import com.task.founding.engineer.model.XRayCandidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for XRayCandidate entities.
 * Provides query methods for candidate analysis.
 */
@Repository
public interface XRayCandidateRepository extends JpaRepository<XRayCandidate, UUID> {

    /**
     * Find all candidates for a specific step.
     */
    List<XRayCandidate> findByStepId(UUID stepId);

    /**
     * Find selected candidates for a step.
     */
    List<XRayCandidate> findByStepIdAndSelectedTrue(UUID stepId);

    /**
     * Find rejected candidates for a step.
     */
    List<XRayCandidate> findByStepIdAndSelectedFalse(UUID stepId);

    /**
     * Count candidates by step.
     */
    long countByStepId(UUID stepId);

    /**
     * Count selected candidates by step.
     */
    long countByStepIdAndSelectedTrue(UUID stepId);

    /**
     * Count rejected candidates by step.
     */
    long countByStepIdAndSelectedFalse(UUID stepId);

    /**
     * Calculate rejection rate for a step.
     */
    @Query("SELECT CASE " +
           "  WHEN COUNT(c) = 0 THEN 0.0 " +
           "  ELSE CAST(COUNT(CASE WHEN c.selected = false THEN 1 END) AS double) / COUNT(c) " +
           "END " +
           "FROM XRayCandidate c WHERE c.stepId = :stepId")
    Double calculateRejectionRate(@Param("stepId") UUID stepId);
}

