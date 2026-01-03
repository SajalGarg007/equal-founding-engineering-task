package com.task.founding.engineer.repository;

import com.task.founding.engineer.model.XRayRun;
import com.task.founding.engineer.enums.RunStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface XRayRunRepository extends JpaRepository<XRayRun, UUID> {


    List<XRayRun> findByPipelineType(String pipelineType);

    /**
     * Find runs by pipeline type and status.
     */
    List<XRayRun> findByPipelineTypeAndStatus(String pipelineType, RunStatus status);

    /**
     * Find runs within a date range.
     */
    List<XRayRun> findByStartedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Find runs by pipeline type within a date range.
     */
    List<XRayRun> findByPipelineTypeAndStartedAtBetween(
            String pipelineType, 
            LocalDateTime start, 
            LocalDateTime end
    );

    /**
     * Find a run with all its steps and candidates loaded (eager fetch).
     */
    @Query("SELECT DISTINCT r FROM XRayRun r " +
           "LEFT JOIN FETCH r.steps s " +
           "LEFT JOIN FETCH s.candidates " +
           "WHERE r.runId = :runId")
    Optional<XRayRun> findByIdWithStepsAndCandidates(@Param("runId") UUID runId);
}

