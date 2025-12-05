package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.ammoEvidence.AmmoEvidenceEntity;
import com.shootingplace.shootingplace.ammoEvidence.AmmoEvidenceRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AmmoEvidenceSQLRepository extends AmmoEvidenceRepository, JpaRepository<AmmoEvidenceEntity,String> {
}