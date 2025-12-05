package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.history.JudgingHistoryEntity;
import com.shootingplace.shootingplace.history.JudgingHistoryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JudgingHistorySQLRepository extends JudgingHistoryRepository, JpaRepository<JudgingHistoryEntity, String> {


}
