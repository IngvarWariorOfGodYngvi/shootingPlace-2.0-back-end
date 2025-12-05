package com.shootingplace.shootingplace.competition;

import java.util.List;
import java.util.Optional;

public interface CompetitionRepository{
    Optional<CompetitionEntity> findByNameEquals(String name);
    boolean existsByName(String name);

    List<CompetitionEntity> findAll();

    CompetitionEntity save(CompetitionEntity entity);

    CompetitionEntity getOne(String uuid);

    Optional<CompetitionEntity> findById(String uuid);

    long count();

    void delete(CompetitionEntity entity);
}
