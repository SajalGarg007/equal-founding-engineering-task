package com.task.founding.engineer.repository;

import com.task.founding.engineer.model.XRayStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface XRayStepRepository extends JpaRepository<XRayStep, UUID> {

    List<XRayStep> findByRunIdOrderByOrderAsc(UUID runId);

    List<XRayStep> findByStepType(String stepType);

    List<XRayStep> findByStepName(String stepName);

    List<XRayStep> findByStepTypeAndStepName(String stepType, String stepName);

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

    @Query("SELECT s FROM XRayStep s " +
           "JOIN FETCH s.run r " +
           "WHERE s.stepType = :stepType")
    List<XRayStep> findByStepTypeWithRun(@Param("stepType") String stepType);
}

