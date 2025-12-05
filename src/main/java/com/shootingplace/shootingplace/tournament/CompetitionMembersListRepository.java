package com.shootingplace.shootingplace.tournament;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CompetitionMembersListRepository{
    List<CompetitionMembersListEntity> findAll();

    CompetitionMembersListEntity save(CompetitionMembersListEntity entity);

    Optional<CompetitionMembersListEntity> findById(String uuid);

    void delete(CompetitionMembersListEntity entity);

    boolean existsById(String uuid);

    CompetitionMembersListEntity getOne(String uuid);
    @Query(nativeQuery = true, value = "SELECT * FROM shootingplace.competition_members_list_entity where attached_to_tournament = (:tournamentUUID)")
    List<CompetitionMembersListEntity> findAllByAttachedToTournament(@Param("tournamentUUID") String tournamentUUID);
}
