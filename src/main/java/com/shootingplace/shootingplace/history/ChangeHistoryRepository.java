package com.shootingplace.shootingplace.history;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChangeHistoryRepository extends JpaRepository<ChangeHistoryEntity, String> {
}
