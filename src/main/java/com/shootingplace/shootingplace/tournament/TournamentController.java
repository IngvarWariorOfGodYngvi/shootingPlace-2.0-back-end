package com.shootingplace.shootingplace.tournament;

import com.shootingplace.shootingplace.enums.UserSubType;
import com.shootingplace.shootingplace.security.RequirePermissions;
import com.shootingplace.shootingplace.tournament.axis.AxisArbiterType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/tournament")
@CrossOrigin
@RequiredArgsConstructor
public class TournamentController {

    private final TournamentService tournamentService;

    @GetMapping("/openTournament")
    public ResponseEntity<?> getOpenTournament() {
        return tournamentService.getOpenTournament();
    }

    @GetMapping("/gunList")
    public ResponseEntity<?> getListOfGunsOnTournament(@RequestParam String tournamentUUID) {
        return ResponseEntity.ok(tournamentService.getListOfGunsOnTournament(tournamentUUID));
    }

    @GetMapping("/getShootersNamesList")
    public ResponseEntity<?> getShootersNamesList(@RequestParam String tournamentUUID) {
        return ResponseEntity.ok(tournamentService.getShootersNamesList(tournamentUUID));
    }

    @GetMapping("/closedList")
    public ResponseEntity<?> getListOfClosedTournaments(Pageable page) {
        return ResponseEntity.ok().body(tournamentService.getClosedTournaments(page));
    }

    @GetMapping("/competitions")
    public ResponseEntity<?> getCompetitionsListInTournament(@RequestParam String tournamentUUID) {
        return ResponseEntity.ok().body(tournamentService.getCompetitionsListInTournament(tournamentUUID));
    }

    @GetMapping("/stat")
    public ResponseEntity<?> getStatistics(@RequestParam String tournamentUUID) {
        return ResponseEntity.ok().body(tournamentService.getStatistics(tournamentUUID));
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkAnyOpenTournament() {
        return ResponseEntity.ok().body(tournamentService.checkAnyOpenTournament());
    }

    @GetMapping("/getJudgingList")
    public ResponseEntity<?> getJudgingList(@RequestParam String firstDate, @RequestParam String secondDate) {
        return tournamentService.getJudgingList(firstDate, secondDate);
    }

    @PostMapping("/")
    public ResponseEntity<String> createNewTournament(@RequestBody TournamentDTO tournament) {
        return tournamentService.createNewTournament(tournament);
    }

    @Transactional
    @PostMapping("/axis")
    public ResponseEntity<?> setAxisLeader(@RequestParam AxisArbiterType axisArbiterType, @RequestParam String axisLeaderID, @RequestParam String axisUUID) {
        return tournamentService.setAxisLeader(axisArbiterType, axisLeaderID, axisUUID);
    }

    @Transactional
    @PostMapping("/axis/arbiter")
    public ResponseEntity<?> setAxisArbiter(@RequestParam AxisArbiterType axisArbiterType,@RequestParam String axisArbiterID, @RequestParam String axisUUID) {
        return tournamentService.setAxisArbiter(axisArbiterType,axisArbiterID, axisUUID);
    }
    @Transactional
    @DeleteMapping("/axis/arbiter")
    public ResponseEntity<?> removeAxisArbiter(@RequestParam AxisArbiterType axisArbiterType,@RequestParam String axisArbiterID, @RequestParam String axisUUID) {
        return tournamentService.removeAxisArbiter(axisArbiterType,axisArbiterID, axisUUID);
    }
    @Transactional
    @PutMapping("/addTechnicalSupport/{tournamentUUID}")
    public ResponseEntity<?> addOthersArbiters(@PathVariable String tournamentUUID, @RequestParam String memberUUID, @RequestParam int id) {

        if (memberUUID != null && !memberUUID.isEmpty() && !memberUUID.equals("null")) {
            return tournamentService.addTechnicalSupport(tournamentUUID, memberUUID);
        }
        if (id > 0) {
            return tournamentService.addOtherTechnicalSupport(tournamentUUID, id);
        } else {
            return ResponseEntity.badRequest().build();
        }

    }
    @Transactional
    @PostMapping("/removeTechnicalSupport/{tournamentUUID}")
    public ResponseEntity<?> removeTechnicalSupportFromTournament(@PathVariable String tournamentUUID, @RequestParam String memberUUID, @RequestParam int id) {

        if (memberUUID != null && !memberUUID.isEmpty() && !memberUUID.equals("null")) {
            return tournamentService.removeTechnicalSupportFromTournament(tournamentUUID, memberUUID);
        }
        if (id > 0) {
            return tournamentService.removeOtherTechnicalSupportFromTournament(tournamentUUID, id);
        } else {
            return ResponseEntity.status(418).body("I'm a teapot");
        }
    }

    @Transactional
    @PostMapping("/removeRTSArbiter/{tournamentUUID}")
    public ResponseEntity<?> removeRTSArbiterFromTournament(@PathVariable String tournamentUUID, @RequestParam String memberUUID, @RequestParam int id) {

        if (memberUUID != null && !memberUUID.isEmpty() && !memberUUID.equals("null")) {
            return tournamentService.removeRTSArbiterFromTournament(tournamentUUID, memberUUID);
        }
        if (id > 0) {
            return tournamentService.removeRTSOtherArbiterFromTournament(tournamentUUID, id);
        } else {
            return ResponseEntity.status(418).body("I'm a teapot");
        }
    }

    @Transactional
    @PatchMapping("/{tournamentUUID}")
    public ResponseEntity<?> closeTournament(@PathVariable String tournamentUUID) {
        return tournamentService.closeTournament(tournamentUUID);
    }

    @Transactional
    @PatchMapping("/open/{tournamentUUID}")
    @RequirePermissions(value = {UserSubType.MANAGEMENT, UserSubType.WORKER})
    public ResponseEntity<?> openTournament(@PathVariable String tournamentUUID) {
        return tournamentService.openTournament(tournamentUUID);
    }

    @Transactional
    @PutMapping("/{tournamentUUID}")
    public ResponseEntity<?> updateTournament(@PathVariable String tournamentUUID, @RequestBody Tournament tournament) {
        return tournamentService.updateTournament(tournamentUUID, tournament);
    }

    @Transactional
    @PutMapping("/addMainArbiter/{tournamentUUID}")
    public ResponseEntity<?> addMainArbiter(@PathVariable String tournamentUUID, @RequestParam String memberUUID, @RequestParam int id) {

        if (memberUUID != null && !memberUUID.isEmpty() && !memberUUID.equals("null")) {
            return tournamentService.addMainArbiter(tournamentUUID, memberUUID);
        }
        if (id > 0) {
            return tournamentService.addOtherMainArbiter(tournamentUUID, id);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @Transactional
    @PutMapping("/addRTSArbiter/{tournamentUUID}")
    public ResponseEntity<?> addRTSArbiter(@PathVariable String tournamentUUID, @RequestParam String memberUUID, @RequestParam int id) {

        if (memberUUID != null && !memberUUID.isEmpty() && !memberUUID.equals("null")) {
            return tournamentService.addRTSArbiter(tournamentUUID, memberUUID);
        }
        if (id > 0) {
            return tournamentService.addOtherRTSArbiter(tournamentUUID, id);
        } else {
            return ResponseEntity.badRequest().build();
        }

    }

    @Transactional
    @PutMapping("/addOthersRTSArbiters/{tournamentUUID}")
    public ResponseEntity<?> addOthersRTSArbiters(@PathVariable String tournamentUUID, @RequestParam String memberUUID, @RequestParam int id) {

        if (memberUUID != null && !memberUUID.isEmpty() && !memberUUID.equals("null")) {
            return tournamentService.addOthersRTSArbiters(tournamentUUID, memberUUID);
        }
        if (id > 0) {
            return tournamentService.addPersonOthersRTSArbiters(tournamentUUID, id);
        } else {
            return ResponseEntity.badRequest().build();
        }

    }

    @Transactional
    @PutMapping("/addCompetition/{tournamentUUID}")
    public ResponseEntity<?> addCompetitionListToTournament(@PathVariable String tournamentUUID, @RequestParam List<String> competitionsUUID) {

        List<String> list = new ArrayList<>();

        competitionsUUID.forEach(e -> list.add(tournamentService.addNewCompetitionListToTournament(tournamentUUID, e)));

        if (!list.isEmpty()) {
            return ResponseEntity.ok(list);
        } else {
            return ResponseEntity.badRequest().body("pusta lista");
        }

    }

    @Transactional
    @DeleteMapping("/delete/{tournamentUUID}")
    @RequirePermissions({UserSubType.ADMIN, UserSubType.MANAGEMENT, UserSubType.SUPER_USER, UserSubType.WORKER})
    public ResponseEntity<?> deleteTournament(@PathVariable String tournamentUUID) {
        return tournamentService.deleteTournament(tournamentUUID);
    }
}
