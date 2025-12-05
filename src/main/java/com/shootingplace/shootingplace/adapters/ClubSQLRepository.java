package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.club.ClubEntity;
import com.shootingplace.shootingplace.club.ClubRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubSQLRepository extends ClubRepository, JpaRepository<ClubEntity,Integer> {

}
