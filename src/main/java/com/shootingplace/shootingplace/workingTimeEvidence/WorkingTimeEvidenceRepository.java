package com.shootingplace.shootingplace.workingTimeEvidence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WorkingTimeEvidenceRepository extends JpaRepository<WorkingTimeEvidenceEntity,String> {

    List<WorkingTimeEvidenceEntity> findAllByIsCloseFalse();

    @Query(nativeQuery = true, value = "SELECT * FROM shootingplace.working_time_evidence_entity where year(stop) = :year and month(stop) = :month")
    List<WorkingTimeEvidenceEntity> findAllByStopQuery(@Param("year") int year, @Param("month") int month);
}
