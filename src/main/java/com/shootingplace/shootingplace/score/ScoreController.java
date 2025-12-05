package com.shootingplace.shootingplace.score;

import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/score")
@CrossOrigin
public class ScoreController {

    private final ScoreService scoreService;

    public ScoreController(ScoreService scoreService) {
        this.scoreService = scoreService;
    }

    @Transactional
    @PutMapping("/set")
    public ResponseEntity<?> setScore(@RequestParam String scoreUUID, @RequestParam float score, @RequestParam float innerTen, @RequestParam float outerTen, @RequestParam int procedures, @RequestParam float miss, @RequestParam float alfa, @RequestParam float charlie, @RequestParam float delta, @Nullable @RequestParam List<Float> series) {
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
