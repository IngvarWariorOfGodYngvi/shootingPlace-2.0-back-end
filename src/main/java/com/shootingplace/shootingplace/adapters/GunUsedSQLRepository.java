package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.armory.GunUsedEntity;
import com.shootingplace.shootingplace.armory.GunUsedRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GunUsedSQLRepository  extends GunUsedRepository, JpaRepository<GunUsedEntity,String> {
}
