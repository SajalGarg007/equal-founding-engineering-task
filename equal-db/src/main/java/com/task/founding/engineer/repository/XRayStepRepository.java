package com.task.founding.engineer.repository;

import com.task.founding.engineer.model.XRayStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for XRayStep entities.
 * Provides query methods for cross-pipeline step analysis.
 */
@Repository
public interface XRayStepRepository extends JpaRepository<XRayStep, UUID> {

    /**
     * Find all steps for a specific run, ordered by step order.
     */
    List<XRayStep> findByRunIdOrderByOrderAsc(UUID runId);

    /**
     * Find steps by step type across all runs.
     */
    List<XRayStep> findByStepType(String stepType);

    /**
     * Find steps by step name across all runs.
     */
    List<XRayStep> findByStepName(String stepName);

    /**
     * Find steps by step type and name.
     */
    List<XRayStep> findByStepTypeAndStepName(String stepType, String stepName);

    /**
     * Find steps with high rejection rates (for filtering steps).
     * This query calculates rejection rate based on candidates.
     */
    @Query("SELECT s FROM XRayStep s " +
           "WHERE s.stepType = :stepType " +
           "AND (SELECT COUNT(c) FROM XRayCandidate c WHERE c.stepId = s.stepId) > 0 " +
           "AND (SELECT CAST(COUNT(c) AS double) FROM XRayCandidate c " +
           "     WHERE c.stepId = s.stepId AND c.selected = false) / " +
           "    (SELECT CAST(COUNT(c) AS double) FROM XRayCandidate c WHERE c.stepId = s.stepId) > :rejectionRate")
    List<XRayStep> findStepsWithHighRejectionRate(
            @Param("stepType") String stepType,
            @Param("rejectionRate") double rejectionRate
    );

    /**
     * Find steps with their parent run information.
     */
    @Query("SELECT s FROM XRayStep s " +
           "JOIN FETCH s.run r " +
           "WHERE s.stepType = :stepType")
    List<XRayStep> findByStepTypeWithRun(@Param("stepType") String stepType);
}

