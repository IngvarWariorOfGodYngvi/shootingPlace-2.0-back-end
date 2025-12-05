package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.history.HistoryEntity;
import com.shootingplace.shootingplace.history.HistoryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistorySQLRepository extends HistoryRepository, JpaRepository<HistoryEntity,String> {

}
