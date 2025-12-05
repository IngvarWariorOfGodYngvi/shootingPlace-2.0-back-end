package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.member.PersonalEvidenceEntity;
import com.shootingplace.shootingplace.member.PersonalEvidenceRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonalEvidenceSQLRepository extends PersonalEvidenceRepository, JpaRepository<PersonalEvidenceEntity, String> {
}
