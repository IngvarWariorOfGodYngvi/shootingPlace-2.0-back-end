package com.shootingplace.shootingplace.armory;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GunStoreRepository extends JpaRepository<GunStoreEntity,String> {
    GunStoreEntity findByTypeName(String typeName);
}
