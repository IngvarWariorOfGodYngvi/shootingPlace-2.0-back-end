package com.shootingplace.shootingplace.club;

import com.shootingplace.shootingplace.enums.UserSubType;
import com.shootingplace.shootingplace.exceptions.NoUserPermissionException;
import com.shootingplace.shootingplace.history.ChangeHistoryService;
import com.shootingplace.shootingplace.security.RequirePermissions;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/club")
@CrossOrigin
public class ClubController {

    private final ClubService clubService;
    private final ChangeHistoryService changeHistoryService;

    public ClubController(ClubService clubService, ChangeHistoryService changeHistoryService) {
        this.clubService = clubService;
        this.changeHistoryService = changeHistoryService;
    }

    @GetMapping("/")
    public ResponseEntity<List<ClubEntity>> getAllClubs() {
        return ResponseEntity.ok(clubService.getAllClubs());
    }

    @GetMapping("/isMotherClubExist")
    public ResponseEntity<?> isMotherClubExists() {
        return ResponseEntity.ok(clubService.isMotherClubExists());
    }

    @GetMapping("/tournament")
    public ResponseEntity<List<String>> getAllClubsToTournament() {
        return ResponseEntity.ok(clubService.getAllClubsToTournament());
    }

    @GetMapping("/member")
    public ResponseEntity<List<ClubEntity>> getAllClubsToMember() {
        return ResponseEntity.ok(clubService.getAllClubsToMember());
    }

    @Transactional
    @PutMapping("/{clubID}")
    public ResponseEntity<?> updateClub(@PathVariable int clubID, @RequestBody Club club) {
        return clubService.updateClub(clubID, club);
    }

    @Transactional
    @PostMapping("/import")
    public ResponseEntity<?> importClub(@RequestBody Club club) {
        return clubService.importCLub(club);
    }

    @Transactional
    @PostMapping("/create")
    public ResponseEntity<?> createNewClub(@RequestBody Club club) {
        return clubService.createNewClub(club);
    }

    @Transactional
    @DeleteMapping("/delete")
    @RequirePermissions(value = {UserSubType.MANAGEMENT})
    public ResponseEntity<?> deleteClub(@RequestParam String id, String pinCode) throws NoUserPermissionException {
        return clubService.deleteClub(Integer.parseInt(id), pinCode);
    }
}
