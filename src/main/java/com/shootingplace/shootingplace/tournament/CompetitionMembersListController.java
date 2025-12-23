package com.shootingplace.shootingplace.tournament;

import com.shootingplace.shootingplace.armory.AmmoUsedService;
import com.shootingplace.shootingplace.armory.ShootingPacketEntity;
import com.shootingplace.shootingplace.armory.ShootingPacketService;
import com.shootingplace.shootingplace.exceptions.NoPersonToAmmunitionException;
import com.shootingplace.shootingplace.score.ScoreService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/competitionMembersList")
@CrossOrigin
@RequiredArgsConstructor
public class CompetitionMembersListController {

    private final CompetitionMembersListService competitionMembersListService;
    private final AmmoUsedService ammoUsedService;
    private final ScoreService scoreService;
    private final CompetitionMembersListRepository competitionMembersListRepository;
    private final ShootingPacketService shootingPacketService;

    @GetMapping("/getShooterStarts")
    public ResponseEntity<?> getShooterStarts( @RequestParam String tournamentUUID, @RequestParam String startNumber) {
        return competitionMembersListService.getShooterStarts(tournamentUUID, startNumber);
    }
    @GetMapping("/memberScores")
    public ResponseEntity<?> getMemberScoresFromCompetitionMemberListUUID(@RequestParam String competitionMemberListUUID) {
        return competitionMembersListService.getMemberScoresFromCompetitionMemberListUUID(competitionMemberListUUID);
    }

    @GetMapping("/tournamentScores")
    public ResponseEntity<?> getTournamentScoresFromUUID(@RequestParam String tournamentUUID) {
        return competitionMembersListService.getTournamentScoresFromUUID(tournamentUUID);
    }

    @GetMapping("/getID")
    public ResponseEntity<String> getIDByName(@RequestParam String name, @RequestParam String tournamentUUID) {
        return ResponseEntity.ok(competitionMembersListService.getCompetitionIDByName(name, tournamentUUID));
    }

    @GetMapping("/getByID")
    public ResponseEntity<?> getCompetitionListByID(@RequestParam String uuid) {
        return ResponseEntity.ok(competitionMembersListService.getCompetitionListByID(uuid));
    }
    @GetMapping("/getFilteredByID")
    public ResponseEntity<?> getFilteredByID(@RequestParam String uuid, @RequestParam String startNumber) {
        return ResponseEntity.ok(competitionMembersListService.getFilteredByID(uuid, startNumber));
    }
    @GetMapping("/getCompetitionDTOByUUID")
    public ResponseEntity<?> getCompetitionNameByUUID(@RequestParam String uuid) {
        return ResponseEntity.ok(competitionMembersListService.getCompetitionDTOByUUID(uuid));
    }

    @GetMapping("/getMemberStarts")
    public List<String> getMemberStartsInTournament(@RequestParam String memberUUID, @RequestParam int otherID, @RequestParam String tournamentUUID) {
        return competitionMembersListService.getMemberStartsInTournament(memberUUID, otherID, tournamentUUID);
    }

    @GetMapping("/getMetricNumber")
    public ResponseEntity<?> getMetricNumber(@RequestParam String legNumber, @RequestParam int otherID, @RequestParam String tournamentUUID) {
        return competitionMembersListService.getMetricNumber(legNumber, otherID, tournamentUUID);
    }

    @GetMapping("/getMemberStartsByLegitimation")
    public List<String> getMemberStartsInTournament(@RequestParam int legNumber, @RequestParam int otherID, @RequestParam String tournamentUUID) {
        return competitionMembersListService.getMemberStartsInTournament(legNumber, otherID, tournamentUUID);
    }

    @GetMapping("/getScoreIdByNumberAndCompetitionName")
    public ResponseEntity<?> getMemberStartsInTournament(@RequestParam int legNumber, @RequestParam int otherID, @RequestParam String tournamentUUID, @RequestParam String competitionName) {
        return ResponseEntity.ok(competitionMembersListService.getScoreID(legNumber, otherID, tournamentUUID, competitionName));
    }

    @Transactional
    @PutMapping("/addMember")
    public ResponseEntity<?> addScoreToCompetitionMembersList(@RequestParam List<String> competitionUUIDList, @RequestParam List<String> addAmmoList, @RequestParam int legitimationNumber, @RequestParam int otherPerson) {
        List<List<String>> list = new ArrayList<>();
        competitionUUIDList.forEach(e -> list.add(competitionMembersListService.addScoreToCompetitionList(e.replaceAll("\\.", ","), legitimationNumber, otherPerson)));
        addAmmoList.forEach(e -> {
            CompetitionMembersListEntity one = competitionMembersListRepository.findById(e).orElseThrow(EntityNotFoundException::new);
            one.setPracticeShots(one.getPracticeShots() != null ? one.getPracticeShots() : 0);
            try {
                if (shootingPacketService.getAllShootingPacketEntities().stream().map(ShootingPacketEntity::getUuid).toList().contains(one.getCaliberUUID())) {
                    shootingPacketService.getAllCalibersFromShootingPacket(one.getCaliberUUID()).forEach(c ->
                    {
                        try {
                            ammoUsedService.addAmmoUsedEntity(c.getCaliberUUID(), legitimationNumber, otherPerson, c.getQuantity());
                        } catch (NoPersonToAmmunitionException ex) {
                            ex.printStackTrace();
                        }
                    });
                } else {
                    ammoUsedService.addAmmoUsedEntity(one.getCaliberUUID(), legitimationNumber, otherPerson, one.getNumberOfShots() + one.getPracticeShots());

                }
            } catch (NoPersonToAmmunitionException ex) {
                ex.printStackTrace();
            }
            String uuid;
            if (legitimationNumber > 0) {
                uuid = one.getScoreList().stream().filter(f -> f.getMember() != null && f.getMember().getLegitimationNumber().equals(legitimationNumber)).findFirst().get().getUuid();
            } else {
                uuid = one.getScoreList().stream().filter(f -> f.getOtherPersonEntity() != null && f.getOtherPersonEntity().getId().equals(otherPerson)).findFirst().get().getUuid();
            }

            scoreService.toggleAmmunitionInScore(uuid);
        });
        if (!list.isEmpty()) {
            List<String> list1 = new ArrayList<>();
            list.forEach(e -> {
                if (e.get(0) == null) {
                    list1.add(e.get(1));
                } else {
                    list1.add(e.getFirst());
                }
            });
            return ResponseEntity.ok(list1);
        } else
            return ResponseEntity.badRequest().body("pusta lista");
    }

    @Transactional
    @PostMapping("/removeMember")
    public ResponseEntity<?> removeMemberFromList(@RequestParam List<String> competitionNameList, @RequestParam int legitimationNumber, @RequestParam int otherPerson) {
        List<String> list = new ArrayList<>();
        competitionNameList.forEach(e -> list.add(competitionMembersListService.removeScoreFromList(e.replaceAll("\\.", ","), legitimationNumber, otherPerson)));
        if (!list.isEmpty()) {
            return ResponseEntity.ok(list);
        } else
            return ResponseEntity.badRequest().body("pusta lista");
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> removeMembersListFromTournament(@RequestParam String competitionUUID, @RequestParam String tournamentUUID) {
        return competitionMembersListService.removeListFromTournament(tournamentUUID, competitionUUID);
    }


}
