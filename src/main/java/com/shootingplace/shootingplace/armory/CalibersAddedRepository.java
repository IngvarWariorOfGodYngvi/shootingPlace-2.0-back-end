package com.shootingplace.shootingplace.armory;

import java.util.List;

public interface CalibersAddedRepository{
    CalibersAddedEntity save(CalibersAddedEntity entity);
    List<CalibersAddedEntity> findAll();
}
