package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.armory.ShootingPacketEntity;
import com.shootingplace.shootingplace.armory.ShootingPacketRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShootingPacketSQLRepository extends ShootingPacketRepository, JpaRepository<ShootingPacketEntity, String> {
}
