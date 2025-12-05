package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.armory.CalibersAddedEntity;
import com.shootingplace.shootingplace.armory.CalibersAddedRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CaliberAddedSQLRepository extends CalibersAddedRepository, JpaRepository<CalibersAddedEntity,String> {
}
