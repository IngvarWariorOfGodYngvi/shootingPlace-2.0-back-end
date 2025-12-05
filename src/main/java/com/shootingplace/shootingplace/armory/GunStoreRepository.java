package com.shootingplace.shootingplace.armory;

import java.util.List;

public interface GunStoreRepository{
    List<GunStoreEntity> findAll();
    GunStoreEntity findByTypeName(String typeName);
    GunStoreEntity save(GunStoreEntity entity);
}
