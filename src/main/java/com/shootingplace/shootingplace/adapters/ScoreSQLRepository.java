package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.score.ScoreEntity;
import com.shootingplace.shootingplace.score.ScoreRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScoreSQLRepository extends ScoreRepository, JpaRepository<ScoreEntity, String> {
}
