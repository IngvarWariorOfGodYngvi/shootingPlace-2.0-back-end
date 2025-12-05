package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.ammoEvidence.AmmoUsedToEvidenceEntity;
import com.shootingplace.shootingplace.ammoEvidence.AmmoUsedToEvidenceEntityRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AmmoUsedToEvidenceEvidenceSQLRepository extends AmmoUsedToEvidenceEntityRepository, JpaRepository<AmmoUsedToEvidenceEntity,String> {
}
