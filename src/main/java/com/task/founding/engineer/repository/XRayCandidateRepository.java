package com.task.founding.engineer.repository;

import com.task.founding.engineer.model.XRayCandidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface XRayCandidateRepository extends JpaRepository<XRayCandidate, UUID> {

    List<XRayCandidate> findByStepId(UUID stepId);

    List<XRayCandidate> findByStepIdAndSelectedTrue(UUID stepId);

    List<XRayCandidate> findByStepIdAndSelectedFalse(UUID stepId);

    long countByStepId(UUID stepId);

    long countByStepIdAndSelectedTrue(UUID stepId);

    long countByStepIdAndSelectedFalse(UUID stepId);

    @Query("SELECT CASE " +
           "  WHEN COUNT(c) = 0 THEN 0.0 " +
           "  ELSE CAST(COUNT(CASE WHEN c.selected = false THEN 1 END) AS double) / COUNT(c) " +
           "END " +
           "FROM XRayCandidate c WHERE c.stepId = :stepId")
    Double calculateRejectionRate(@Param("stepId") UUID stepId);
}

