package com.shootingplace.shootingplace.competition;

import com.shootingplace.shootingplace.enums.UserSubType;
import com.shootingplace.shootingplace.exceptions.NoUserPermissionException;
import com.shootingplace.shootingplace.history.ChangeHistoryService;
import com.shootingplace.shootingplace.security.RequirePermissions;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/competition")
@CrossOrigin
public class CompetitionController {

    private final CompetitionService competitionService;
    private final ChangeHistoryService changeHistoryService;

    public CompetitionController(CompetitionService competitionService, ChangeHistoryService changeHistoryService) {
        this.competitionService = competitionService;
        this.changeHistoryService = changeHistoryService;
    }

    @GetMapping("/")
    public ResponseEntity<List<CompetitionEntity>> getAllCompetitions() {
        return ResponseEntity.ok(competitionService.getAllCompetitions());
    }

    @GetMapping("/getCountingMethods")
    public ResponseEntity<?> getCountingMethods() {
        return ResponseEntity.ok(competitionService.getCountingMethods());
    }

    @GetMapping("/getDisciplines")
    public ResponseEntity<?> getDisciplines() {
        return ResponseEntity.ok(competitionService.getDisciplines());
    }

    @GetMapping("/getCompetitionTypes")
    public ResponseEntity<?> getCompetitionTypes() {
        return ResponseEntity.ok(competitionService.getCompetitionTypes());
    }

    @GetMapping("/competitionMemberListUUID")
    public ResponseEntity<?> getCompetitionMemberList(@RequestParam String competitionMembersListUUID) {
        return competitionService.getCompetitionMemberList(competitionMembersListUUID);
    }

    @Transactional
    @PostMapping("")
    public ResponseEntity<?> createCompetition(@RequestBody Competition competition) {
        if (competition.getName().isEmpty()) {
            return ResponseEntity.badRequest().body("Wymyśl jakąś nazwę");
        }
        return competitionService.createNewCompetition(competition);
    }

    @Transactional
    @PutMapping("/update")
    @RequirePermissions(value = {UserSubType.MANAGEMENT, UserSubType.WORKER})
    public ResponseEntity<?> updateCompetition(@RequestParam String uuid, @RequestBody Competition competition, @RequestParam String pinCode) throws NoUserPermissionException {
        return competitionService.updateCompetition(uuid, competition, pinCode);
    }

    @Transactional
    @DeleteMapping("/delete")
    @RequirePermissions(value = {UserSubType.MANAGEMENT, UserSubType.WORKER})
    public ResponseEntity<?> deleteCompetition(@RequestParam String uuid, @RequestParam String pinCode) throws NoUserPermissionException {
        return competitionService.deleteCompetition(uuid, pinCode);
    }
}
