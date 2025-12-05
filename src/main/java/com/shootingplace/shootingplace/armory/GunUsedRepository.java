package com.shootingplace.shootingplace.armory;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface GunUsedRepository {
    List<GunUsedEntity> findAll();

    GunUsedEntity save(GunUsedEntity build);

    GunUsedEntity getOne(String uuid);

    @Query(nativeQuery = true, value = "Select * from shootingplace.gun_used_entity where (acceptance_date between (:firstDate) and (:secondDate)) order by acceptance_date")
    List<GunUsedEntity> findAllAcceptanceDayBeetween(@Param("firstDate") LocalDate firstDate, @Param("secondDate") LocalDate secondDate);

}
