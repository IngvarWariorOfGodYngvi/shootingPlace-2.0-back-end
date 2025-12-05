package com.shootingplace.shootingplace.armory;

import java.util.List;
import java.util.Optional;

public interface GunRepository{
    Optional<GunEntity> findByImgUUID(String uuid);

    Optional<GunEntity> findByBarcode(String barcode);

    List<GunEntity> findAll();

    GunEntity save(GunEntity entity);

    Optional<GunEntity> findById(String uuid);

    GunEntity getOne(String uuid);
    GunEntity getByBarcode(String barcode);
    List<GunEntity> findAllByInUseStatus(String status);


}
