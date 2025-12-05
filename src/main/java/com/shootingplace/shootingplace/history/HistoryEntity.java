package com.shootingplace.shootingplace.history;

import com.shootingplace.shootingplace.contributions.ContributionEntity;
import com.shootingplace.shootingplace.email.SentEmail;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HistoryEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    private String uuid;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("paymentDay DESC,validThru DESC")
    private List<ContributionEntity> contributionList;
    private String[] licenseHistory;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("date DESC")
    private List<LicensePaymentHistoryEntity> licensePaymentHistory;

    private Boolean patentFirstRecord;
    private LocalDate[] patentDay;

    private Integer pistolCounter;
    private Integer rifleCounter;
    private Integer shotgunCounter;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("date DESC")
    private List<CompetitionHistoryEntity> competitionHistory;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("date DESC")
    private List<JudgingHistoryEntity> judgingHistory;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("sentAt DESC")
    private Set<SentEmail> sentEmailsHistory;

    public String getUuid() {
        return uuid;
    }

    public Set<SentEmail> getSentEmailsHistory() {
        return sentEmailsHistory;
    }

    public void setSentEmailsHistory(Set<SentEmail> sentEmailsHistory) {
        this.sentEmailsHistory = sentEmailsHistory;
    }

    public List<ContributionEntity> getContributionList() {
        return contributionList;
    }

    public void setContributionList(List<ContributionEntity> contributionsList) {
        this.contributionList = contributionsList;
    }

    public String[] getLicenseHistory() {
        return licenseHistory;
    }

    public void setLicenseHistory(String[] licenseHistory) {
        this.licenseHistory = licenseHistory;
    }

    public List<LicensePaymentHistoryEntity> getLicensePaymentHistory() {
        return licensePaymentHistory;
    }

    public void setLicensePaymentHistory(List<LicensePaymentHistoryEntity> licensePaymentHistory) {
        this.licensePaymentHistory = licensePaymentHistory;
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

    public List<CompetitionHistoryEntity> getCompetitionHistory() {
        return competitionHistory;
    }

    public void setCompetitionHistory(List<CompetitionHistoryEntity> competitionHistory) {
        this.competitionHistory = competitionHistory;
    }

    public List<JudgingHistoryEntity> getJudgingHistory() {
        return judgingHistory;
    }

    public void setJudgingHistory(List<JudgingHistoryEntity> judgingHistory) {
        this.judgingHistory = judgingHistory;
    }
}
