package com.shootingplace.shootingplace.history;

import java.util.Optional;

public interface CompetitionHistoryRepository{
    CompetitionHistoryEntity save(CompetitionHistoryEntity entity);

    Optional<CompetitionHistoryEntity> findById(String uuid);

    void delete(CompetitionHistoryEntity competitionHistoryEntity);
}
