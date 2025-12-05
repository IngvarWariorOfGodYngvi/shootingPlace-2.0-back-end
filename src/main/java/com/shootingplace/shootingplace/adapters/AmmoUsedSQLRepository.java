package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.ammoEvidence.AmmoUsedEntity;
import com.shootingplace.shootingplace.ammoEvidence.AmmoUsedRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AmmoUsedSQLRepository extends AmmoUsedRepository, JpaRepository<AmmoUsedEntity,String> {
}
