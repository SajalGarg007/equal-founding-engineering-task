package com.task.founding.engineer.repository;

import com.task.founding.engineer.model.XRayStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface XRayStepRepository
        extends JpaRepository<XRayStep, UUID>
{

    List<XRayStep> findByRunIdOrderByOrderAsc(UUID runId);

    List<XRayStep> findByStepType(String stepType);

    List<XRayStep> findByStepName(String stepName);

    List<XRayStep> findByStepTypeAndStepName(String stepType, String stepName);

    @Query(value = "SELECT s.* FROM xray_steps s " +
           "WHERE s.step_type = :stepType " +
           "AND EXISTS (SELECT 1 FROM xray_candidates c WHERE c.step_id = s.step_id) " +
           "AND (SELECT COUNT(c.candidate_id)::numeric FROM xray_candidates c " +
           "     WHERE c.step_id = s.step_id AND c.selected = false) / " +
           "    NULLIF((SELECT COUNT(c.candidate_id) FROM xray_candidates c WHERE c.step_id = s.step_id), 0) > :rejectionRate",
           nativeQuery = true)
    List<XRayStep> findStepsWithHighRejectionRate(
            @Param("stepType") String stepType,
            @Param("rejectionRate") double rejectionRate
    );

    @Query("SELECT s FROM XRayStep s " +
           "JOIN FETCH s.run r " +
           "WHERE s.stepType = :stepType")
    List<XRayStep> findByStepTypeWithRun(@Param("stepType") String stepType);

    @Query("SELECT s FROM XRayStep s " +
            "JOIN s.run r " +
            "WHERE s.stepType = 'filter' " +
            "AND (:pipelineType IS NULL OR r.pipelineType = :pipelineType) " +
            "AND (:startDate IS NULL OR s.startedAt >= :startDate) " +
            "AND (:endDate IS NULL OR s.startedAt <= :endDate)")
    List<XRayStep> findFilteringSteps(
            @Param("pipelineType") String pipelineType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}

