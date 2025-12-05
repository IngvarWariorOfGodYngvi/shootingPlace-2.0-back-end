package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.history.UsedHistoryEntity;
import com.shootingplace.shootingplace.history.UsedHistoryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsedHistorySQLRepository extends UsedHistoryRepository, JpaRepository<UsedHistoryEntity,String> {

}
