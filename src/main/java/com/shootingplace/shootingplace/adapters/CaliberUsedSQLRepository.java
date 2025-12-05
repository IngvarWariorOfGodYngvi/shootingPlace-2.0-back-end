package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.armory.CaliberUsedEntity;
import com.shootingplace.shootingplace.armory.CaliberUsedRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CaliberUsedSQLRepository extends CaliberUsedRepository, JpaRepository<CaliberUsedEntity,String> {
}
