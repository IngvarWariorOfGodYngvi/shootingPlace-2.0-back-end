package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.competition.CompetitionEntity;
import com.shootingplace.shootingplace.competition.CompetitionRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompetitionSQLRepository  extends CompetitionRepository,JpaRepository<CompetitionEntity, String> {
}
