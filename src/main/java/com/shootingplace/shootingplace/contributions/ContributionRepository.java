package com.shootingplace.shootingplace.contributions;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ContributionRepository{

    ContributionEntity save(ContributionEntity entity);

    void delete(ContributionEntity entity);

    Optional<ContributionEntity> findById(String contributionUUID);

    List<ContributionEntity> findAll();

    ContributionEntity getOne(String contributionUUID);
    @Query(nativeQuery = true, value = "Select * from shootingplace.contribution_entity where (payment_day between (:firstDate) and (:secondDate)) order by payment_day")
    List<ContributionEntity> getAllPaymentDayBetween(@Param("firstDate")LocalDate firstDate, @Param("secondDate") LocalDate secondDate);

    boolean existsById(String uuid);
}
