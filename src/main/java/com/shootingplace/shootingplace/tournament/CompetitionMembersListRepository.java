package com.shootingplace.shootingplace.tournament;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CompetitionMembersListRepository extends JpaRepository<CompetitionMembersListEntity, String> {
    @Query(nativeQuery = true, value = "SELECT * FROM shootingplace.competition_members_list_entity where attached_to_tournament = (:tournamentUUID)")
    List<CompetitionMembersListEntity> findAllByAttachedToTournament(@Param("tournamentUUID") String tournamentUUID);
}
