package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.ammoEvidence.AmmoInEvidenceEntity;
import com.shootingplace.shootingplace.ammoEvidence.AmmoInEvidenceRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AmmoInEvidenceSQLRepository extends AmmoInEvidenceRepository, JpaRepository<AmmoInEvidenceEntity,String> {
}
