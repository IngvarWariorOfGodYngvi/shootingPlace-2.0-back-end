package com.shootingplace.shootingplace.history;

import java.util.List;
import java.util.Optional;

public interface HistoryRepository{
    HistoryEntity save(HistoryEntity entity);

    Optional<HistoryEntity> findById(String uuid);

    List<HistoryEntity> findAll();

    void delete(HistoryEntity e);

    HistoryEntity getByUuid(String uuid);
}
