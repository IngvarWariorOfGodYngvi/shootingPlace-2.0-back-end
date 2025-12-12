package com.shootingplace.shootingplace.club;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubRepository extends JpaRepository<ClubEntity, Integer> {
    boolean existsByShortName(String shortName);

    ClubEntity findByShortName(String shortName);
//    List<ClubEntity> findAll();
//
//    boolean existsById(int id);
//
//    Optional<ClubEntity> findById(int id);
//
//    ClubEntity save(ClubEntity entity);
//
//    ClubEntity getOne(int id);
//
//    boolean existsByShortName(String clubName);
//
//    ClubEntity findByShortName(String shortName);
//
//    long count();
//
//    void delete(ClubEntity entity);
}
