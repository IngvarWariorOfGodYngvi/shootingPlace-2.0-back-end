package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.history.CompetitionHistoryEntity;
import com.shootingplace.shootingplace.history.CompetitionHistoryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompetitionHistorySQLRepository extends CompetitionHistoryRepository, JpaRepository<CompetitionHistoryEntity, String> {
}
