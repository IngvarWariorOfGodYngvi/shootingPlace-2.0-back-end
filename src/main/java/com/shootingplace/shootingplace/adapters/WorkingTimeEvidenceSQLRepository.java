package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.workingTimeEvidence.WorkingTimeEvidenceEntity;
import com.shootingplace.shootingplace.workingTimeEvidence.WorkingTimeEvidenceRepository;
import org.springframework.data.jpa.repository.JpaRepository;

interface WorkingTimeEvidenceSQLRepository extends WorkingTimeEvidenceRepository, JpaRepository<WorkingTimeEvidenceEntity, String> {
}
