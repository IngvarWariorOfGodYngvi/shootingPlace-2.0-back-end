package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.armory.GunStoreEntity;
import com.shootingplace.shootingplace.armory.GunStoreRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GunStoreSQLRepository extends GunStoreRepository, JpaRepository<GunStoreEntity,String> {
}
