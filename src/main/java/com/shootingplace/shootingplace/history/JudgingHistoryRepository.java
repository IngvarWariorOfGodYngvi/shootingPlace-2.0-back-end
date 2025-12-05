package com.shootingplace.shootingplace.history;

import java.time.LocalDate;
import java.util.List;

public interface JudgingHistoryRepository{
    JudgingHistoryEntity save(JudgingHistoryEntity entity);

    void delete(JudgingHistoryEntity entity);

    List<JudgingHistoryEntity> findAll();

    List<JudgingHistoryEntity> findAllByDateBetween(LocalDate firstDate, LocalDate secondDate);

}
