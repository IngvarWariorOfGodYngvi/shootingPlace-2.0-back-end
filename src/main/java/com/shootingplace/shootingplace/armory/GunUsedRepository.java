package com.shootingplace.shootingplace.armory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface GunUsedRepository extends JpaRepository<GunUsedEntity, String> {

    @Query(nativeQuery = true, value = "SELECT * FROM shootingplace.gun_used_entity WHERE acceptance_date BETWEEN :firstDate AND :secondDate ORDER BY acceptance_date")
    List<GunUsedEntity> findAllByAcceptanceDateBetween(@Param("firstDate") LocalDate firstDate, @Param("secondDate") LocalDate secondDate);

}
