package com.shootingplace.shootingplace.armory;

import java.util.List;

public interface ShootingPacketRepository {
    List<ShootingPacketEntity> findAll();

    ShootingPacketEntity save(ShootingPacketEntity entity);

    ShootingPacketEntity getOne(String uuid);

    void delete(ShootingPacketEntity entity);
}
