package com.shootingplace.shootingplace.armory;

import java.util.List;

public interface CaliberForShootingPacketRepository {
    List<CaliberForShootingPacketEntity> findAll();

    CaliberForShootingPacketEntity save(CaliberForShootingPacketEntity entity);
}
