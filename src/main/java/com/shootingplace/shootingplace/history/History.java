package com.shootingplace.shootingplace.history;

import com.shootingplace.shootingplace.contributions.Contribution;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;


@Builder
@NoArgsConstructor
@AllArgsConstructor
public class History {

    private List<Contribution> contributionList;
    private String[] licenseHistory;
    private List<LicensePaymentHistory> licensePaymentHistory;

    private Boolean patentFirstRecord;
    private LocalDate[] patentDay;

    private Integer pistolCounter;
    private Integer rifleCounter;
    private Integer shotgunCounter;
    private List<CompetitionHistory> competitionHistory;

    private List<JudgingHistoryEntity> judgingHistory;

    public String[] getLicenseHistory() {
        return licenseHistory;
    }

    public void setLicenseHistory(String[] licenseHistory) {
        this.licenseHistory = licenseHistory;
    }

    public List<LicensePaymentHistory> getLicensePaymentHistory() {
        return licensePaymentHistory;
    }

    public void setLicensePaymentHistory(List<LicensePaymentHistory> licensePaymentHistory) {
        this.licensePaymentHistory = licensePaymentHistory;
    }

    public List<Contribution> getContributionList() {
        return contributionList;
    }

    public List<CompetitionHistory> getCompetitionHistory() {
        return competitionHistory;
    }

    public Boolean getPatentFirstRecord() {
        return patentFirstRecord;
    }

    public void setPatentFirstRecord(Boolean patentFirstRecord) {
        this.patentFirstRecord = patentFirstRecord;
    }

    public LocalDate[] getPatentDay() {
        return patentDay;
    }

    public void setPatentDay(LocalDate[] patentDay) {
        this.patentDay = patentDay;
    }

    public Integer getPistolCounter() {
        return pistolCounter;
    }

    public void setPistolCounter(Integer pistolCounter) {
        this.pistolCounter = pistolCounter;
    }

    public Integer getRifleCounter() {
        return rifleCounter;
    }

    public void setRifleCounter(Integer rifleCounter) {
        this.rifleCounter = rifleCounter;
    }

    public Integer getShotgunCounter() {
        return shotgunCounter;
    }

    public void setShotgunCounter(Integer shotgunCounter) {
        this.shotgunCounter = shotgunCounter;
    }

    @Override
    public String toString() {
        return "History{" +
                "contributionList=" + contributionList +
                ", licenseHistory=" + Arrays.toString(licenseHistory) +
                ", licensePaymentHistory=" + licensePaymentHistory +
                ", patentFirstRecord=" + patentFirstRecord +
                ", patentDay=" + Arrays.toString(patentDay) +
                ", pistolCounter=" + pistolCounter +
                ", rifleCounter=" + rifleCounter +
                ", shotgunCounter=" + shotgunCounter +
                ", competitionHistory=" + competitionHistory +
                ", judgingHistory=" + judgingHistory +
                '}';
    }
}
