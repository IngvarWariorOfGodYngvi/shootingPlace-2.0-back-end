package com.shootingplace.shootingplace.bookOfRegistrationOfStayAtTheShootingPlace;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;


public interface RegistrationRecordRepository extends JpaRepository<RegistrationRecordEntity, String> {
    @Query(nativeQuery = true, value = "Select * from shootingplace.registration_record_entity where(date_time between (:start) and (:stop))")
    List<RegistrationRecordEntity> findAllBetweenDate(@Param("start") LocalDateTime start, @Param("stop") LocalDateTime stop);

    List<RegistrationRecordEntity> findAllByEndDateTimeNull();
}
