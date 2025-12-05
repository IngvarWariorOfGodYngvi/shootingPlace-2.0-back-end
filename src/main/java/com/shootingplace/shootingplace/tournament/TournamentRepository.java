package com.shootingplace.shootingplace.tournament;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface TournamentRepository {
    Optional<TournamentEntity> findById(String uuid);

    TournamentEntity save(TournamentEntity entity);

    List<TournamentEntity> findAll();
    Page<TournamentEntity> findAllByOpenIsFalse(Pageable page);

    boolean existsById(String uuid);

    void delete(TournamentEntity entity);

    TournamentEntity getOne(String tournamentUUID);

    TournamentEntity findByOpenIsTrue();
}
