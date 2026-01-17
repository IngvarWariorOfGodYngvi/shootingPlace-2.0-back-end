package com.shootingplace.shootingplace.changeHistory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChangeHistoryRepository extends JpaRepository<ChangeHistoryEntity, String> {

    List<ChangeHistoryEntity> findAllByUserEntity_UuidOrderByDayNowDescTimeNowDesc(
            String userUuid
    );
}
