package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.armory.GunEntity;
import com.shootingplace.shootingplace.armory.GunRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GunSQLRepository extends GunRepository, JpaRepository<GunEntity,String> {
}
