package com.shootingplace.shootingplace.history;

import java.util.List;

public interface ChangeHistoryRepository{
    ChangeHistoryEntity save(ChangeHistoryEntity entity);

    List<ChangeHistoryEntity> findAll();
}
