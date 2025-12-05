package com.shootingplace.shootingplace.ammoEvidence;

import java.util.List;

public interface AmmoUsedRepository{
    AmmoUsedEntity save(AmmoUsedEntity entity);

    List<AmmoUsedEntity> findAll();
}
