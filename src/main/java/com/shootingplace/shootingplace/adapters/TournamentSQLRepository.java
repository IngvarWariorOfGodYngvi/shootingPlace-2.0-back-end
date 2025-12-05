package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.tournament.TournamentEntity;
import com.shootingplace.shootingplace.tournament.TournamentRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TournamentSQLRepository extends TournamentRepository, JpaRepository<TournamentEntity,String> {
}
