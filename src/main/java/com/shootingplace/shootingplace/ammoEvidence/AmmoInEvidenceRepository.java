package com.shootingplace.shootingplace.ammoEvidence;

import java.util.List;

public interface AmmoInEvidenceRepository{
    AmmoInEvidenceEntity save(AmmoInEvidenceEntity entity);

    void delete(AmmoInEvidenceEntity entity);

    List<AmmoInEvidenceEntity> findAll();

    AmmoInEvidenceEntity getOne(String uuid);
}
