package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.armory.CaliberEntity;
import com.shootingplace.shootingplace.armory.CaliberRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CaliberSQLRepository extends CaliberRepository, JpaRepository<CaliberEntity,String> {
}
