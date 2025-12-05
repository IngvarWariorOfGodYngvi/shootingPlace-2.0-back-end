package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.armory.CaliberForShootingPacketEntity;
import com.shootingplace.shootingplace.armory.CaliberForShootingPacketRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CaliberForShootingPacketSQLRepository extends CaliberForShootingPacketRepository, JpaRepository<CaliberForShootingPacketEntity,String> {
}
