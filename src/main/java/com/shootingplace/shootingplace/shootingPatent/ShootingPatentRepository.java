package com.shootingplace.shootingplace.shootingPatent;

import java.util.List;
import java.util.Optional;

public interface ShootingPatentRepository{
    Optional<ShootingPatentEntity> findByPatentNumber(String number);

    List<ShootingPatentEntity> findByPatentNumberIsNotNull();

    ShootingPatentEntity save(ShootingPatentEntity entity);
}
