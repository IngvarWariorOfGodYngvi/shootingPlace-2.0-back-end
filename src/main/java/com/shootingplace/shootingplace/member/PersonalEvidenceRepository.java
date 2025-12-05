package com.shootingplace.shootingplace.member;

import java.util.List;

public interface PersonalEvidenceRepository  {
    PersonalEvidenceEntity save(PersonalEvidenceEntity entity);

    List<PersonalEvidenceEntity> findAll();
}
