package com.shootingplace.shootingplace.workingTimeEvidence;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WorkingTimeEvidenceRepository {

    WorkingTimeEvidenceEntity save(WorkingTimeEvidenceEntity entity);

    List<WorkingTimeEvidenceEntity> findAll();
    List<WorkingTimeEvidenceEntity> findAllByIsCloseFalse();

    @Query(nativeQuery = true, value = "SELECT * FROM shootingplace.working_time_evidence_entity where year(stop) = :year and month(stop) = :month")
    List<WorkingTimeEvidenceEntity> findAllByStopQuery(@Param("year") int year, @Param("month") int month);

    Optional<WorkingTimeEvidenceEntity> findById(String e);

    boolean existsByIsCloseFalse();

    WorkingTimeEvidenceEntity getOne(String uuid);
}
