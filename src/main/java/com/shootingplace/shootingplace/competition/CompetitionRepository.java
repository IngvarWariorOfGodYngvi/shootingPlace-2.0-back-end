package com.shootingplace.shootingplace.competition;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompetitionRepository extends JpaRepository<CompetitionEntity, String> {
    Optional<CompetitionEntity> findByNameEquals(String name);
    boolean existsByName(String name);

    long count();
}
