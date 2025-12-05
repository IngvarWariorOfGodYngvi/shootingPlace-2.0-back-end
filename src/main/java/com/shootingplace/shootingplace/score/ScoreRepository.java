package com.shootingplace.shootingplace.score;

import java.util.Optional;

public interface ScoreRepository{
    ScoreEntity save(ScoreEntity entity);

    boolean existsById(String scoreUUID);

    ScoreEntity getOne(String scoreUUID);

    Optional<ScoreEntity> findById(String scoreUUID);
}
