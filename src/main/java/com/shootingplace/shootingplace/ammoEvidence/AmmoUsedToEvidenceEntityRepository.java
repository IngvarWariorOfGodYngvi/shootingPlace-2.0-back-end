package com.shootingplace.shootingplace.ammoEvidence;

import java.util.List;

public interface AmmoUsedToEvidenceEntityRepository{
    AmmoUsedToEvidenceEntity save(AmmoUsedToEvidenceEntity entity);

    List<AmmoUsedToEvidenceEntity> findAll();

    void delete(AmmoUsedToEvidenceEntity entity);
}
