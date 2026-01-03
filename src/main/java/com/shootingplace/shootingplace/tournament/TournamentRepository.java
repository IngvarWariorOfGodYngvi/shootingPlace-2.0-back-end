package com.shootingplace.shootingplace.tournament;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TournamentRepository extends JpaRepository<TournamentEntity,String> {

    Page<TournamentEntity> findAllByOpenIsFalse(Pageable page);

    TournamentEntity findByOpenIsTrue();

    boolean existsByOpenTrue();
}
