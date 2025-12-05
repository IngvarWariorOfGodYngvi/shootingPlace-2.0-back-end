package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.history.ChangeHistoryEntity;
import com.shootingplace.shootingplace.history.ChangeHistoryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChangeHistorySQLRepository extends ChangeHistoryRepository, JpaRepository<ChangeHistoryEntity,String> {
}
