package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.shootingPatent.ShootingPatentEntity;
import com.shootingplace.shootingplace.shootingPatent.ShootingPatentRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShootingPatentSQLRepository extends ShootingPatentRepository, JpaRepository<ShootingPatentEntity,String> {
}
