package com.shootingplace.shootingplace.club;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubRepository extends JpaRepository<ClubEntity, Integer> {
    boolean existsByShortName(String shortName);

    ClubEntity findByShortName(String shortName);
}
