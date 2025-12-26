package com.shootingplace.shootingplace.score;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/score")
@CrossOrigin
@RequiredArgsConstructor
public class ScoreController {

    private final ScoreService scoreService;

    @Transactional
    @PutMapping("/set")
    public ResponseEntity<?> setScore(@RequestParam String scoreUUID, @RequestParam(required = false) Float score, @RequestParam(required = false) Float innerTen, @RequestParam(required = false) Float outerTen, @RequestParam(required = false) Integer procedures, @RequestParam(required = false) Float miss, @RequestParam(required = false) Float alfa, @RequestParam(required = false) Float charlie, @RequestParam(required = false) Float delta, @Nullable @RequestParam(required = false) List<Float> series) {
        return scoreService.setScore(scoreUUID, score, innerTen, outerTen, alfa, charlie, delta, procedures, miss, series);
    }

    @Transactional
    @PutMapping("/forceSetScore")
    public ResponseEntity<?> forceSetScore(@RequestParam String scoreUUID, @RequestParam float score) {
        return scoreService.forceSetScore(scoreUUID, score);
    }

    @Transactional
    @PatchMapping("/ammo")
    public ResponseEntity<?> toggleAmmunitionInScore(@RequestParam String scoreUUID) {

        return scoreService.toggleAmmunitionInScore(scoreUUID);
    }

    @Transactional
    @PatchMapping("/gun")
    public ResponseEntity<?> toggleGunInScore(@RequestParam String scoreUUID) {

        return scoreService.toggleGunInScore(scoreUUID);
    }

    @Transactional
    @PatchMapping("/dnf")
    public ResponseEntity<?> toggleDnfScore(@RequestParam String scoreUUID) {

        return scoreService.toggleDnfScore(scoreUUID);
    }

    @Transactional
    @PatchMapping("/dsq")
    public ResponseEntity<?> toggleDsqScore(@RequestParam String scoreUUID) {

        return scoreService.toggleDsqScore(scoreUUID);
    }

    @Transactional
    @PatchMapping("/pk")
    public ResponseEntity<?> togglePkScore(@RequestParam String scoreUUID) {

        return scoreService.togglePkScore(scoreUUID);
    }

}
