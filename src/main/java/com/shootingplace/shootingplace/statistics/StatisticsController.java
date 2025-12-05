package com.shootingplace.shootingplace.statistics;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/statistics")
@CrossOrigin
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }


    @GetMapping("/contributionSum")
    public ResponseEntity<?> getContributionSum(@RequestParam String firstDate, @RequestParam String secondDate) {
        LocalDate parseFirstDate = LocalDate.parse(firstDate);
        LocalDate parseSecondDate = LocalDate.parse(secondDate);
        return ResponseEntity.ok(statisticsService.getContributionSum(parseFirstDate, parseSecondDate));
    }
    @GetMapping("/weekBirthdayList")
    public ResponseEntity<?> getWeekBirthdayList() {
        return ResponseEntity.ok(statisticsService.getWeekBirthdayList());
    }

    @GetMapping("/joinDateSum")
    public ResponseEntity<?> getJoinDateSum(@RequestParam String firstDate, @RequestParam String secondDate) {
        LocalDate parseFirstDate = LocalDate.parse(firstDate);
        LocalDate parseSecondDate = LocalDate.parse(secondDate);
        return ResponseEntity.ok(statisticsService.getJoinDateSum(parseFirstDate, parseSecondDate));
    }

    @GetMapping("/erasedSum")
    public ResponseEntity<?> getErasedMembersSum(@RequestParam String firstDate, @RequestParam String secondDate) {
        LocalDate parseFirstDate = LocalDate.parse(firstDate);
        LocalDate parseSecondDate = LocalDate.parse(secondDate);
        return ResponseEntity.ok(statisticsService.getErasedMembersSum(parseFirstDate, parseSecondDate));
    }

    @GetMapping("/licenseSum")
    public ResponseEntity<?> getLicenseSum(@RequestParam String firstDate, @RequestParam String secondDate) {
        LocalDate parseFirstDate = LocalDate.parse(firstDate);
        LocalDate parseSecondDate = LocalDate.parse(secondDate);
        return ResponseEntity.ok(statisticsService.getLicenseSum(parseFirstDate, parseSecondDate));
    }

    @GetMapping("/maxLegNumber")
    public ResponseEntity<?> getMaxLegNumber() {
        return ResponseEntity.ok(statisticsService.getMaxLegNumber());
    }

    @GetMapping("/actualYearMemberCounts")
    public ResponseEntity<?> getActualYearMemberCounts() {
        return ResponseEntity.ok(statisticsService.getActualYearMemberCounts());
    }

    @GetMapping("/memberAmmoTakesInTime")
    public ResponseEntity<?> getMembersAmmoTakesInTime(@RequestParam String firstDate, @RequestParam String secondDate) {
        LocalDate parseFirstDate = LocalDate.parse(firstDate);
        LocalDate parseSecondDate = LocalDate.parse(secondDate);
        return ResponseEntity.ok(statisticsService.getMembersAmmoTakesInTime(parseFirstDate, parseSecondDate));
    }
    @GetMapping("/otherAmmoTakesInTime")
    public ResponseEntity<?> getOthersAmmoTakesInTime(@RequestParam String firstDate, @RequestParam String secondDate) {
        LocalDate parseFirstDate = LocalDate.parse(firstDate);
        LocalDate parseSecondDate = LocalDate.parse(secondDate);
        return ResponseEntity.ok(statisticsService.getOthersAmmoTakesInTime(parseFirstDate, parseSecondDate));
    }

    @GetMapping("/personal")
    public ResponseEntity<?> getPersonalStatistic(@RequestParam String uuid) {
        return statisticsService.getPersonalStatistics(uuid);
    }

    @GetMapping("/highStarts")
    public ResponseEntity<?> getHighStatisticsCompetitions() {
        return statisticsService.getHighStatisticsCompetitions();
    }

    @GetMapping("/membersQuantity" )
    public List<Long> getMembersQuantity() {
        return statisticsService.getMembersQuantity();
    }

    @GetMapping("/highStartsCompetitors")
    public ResponseEntity<?> getTop10Competitors() {
        return statisticsService.getTop10Competitors();
    }

    @GetMapping("/highContributions")
    public ResponseEntity<?> getTop10MembersWithTheMostMembershipContributions() {
        return statisticsService.getTop10MembersWithTheMostMembershipContributions();
    }

    @GetMapping("/highCompetitionPoints")
    public ResponseEntity<?> getTop10CompetitionPoints() {
        return statisticsService.getTop10CompetitionPoints();
    }
}
