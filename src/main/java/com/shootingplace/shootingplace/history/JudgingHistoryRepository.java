package com.shootingplace.shootingplace.history;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface JudgingHistoryRepository extends JpaRepository<JudgingHistoryEntity, String> {

    List<JudgingHistoryEntity> findAllByDateBetween(LocalDate firstDate, LocalDate secondDate);

}
