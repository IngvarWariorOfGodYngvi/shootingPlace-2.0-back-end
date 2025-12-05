package com.shootingplace.shootingplace.armory;

import java.util.List;
import java.util.Optional;

public interface CaliberRepository{
    List<CaliberEntity> findAll();

    CaliberEntity save(CaliberEntity entity);

    Optional<CaliberEntity> findById(String caliberUUID);

    CaliberEntity getOne(String caliberUUID);
}
