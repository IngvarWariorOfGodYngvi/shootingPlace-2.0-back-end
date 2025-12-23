package com.shootingplace.shootingplace.history;

import com.shootingplace.shootingplace.contributions.ContributionEntity;
import com.shootingplace.shootingplace.email.SentEmail;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HistoryEntity {
    @Id
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

    public void setSentEmailsHistory(Set<SentEmail> sentEmailsHistory) {
        this.sentEmailsHistory = sentEmailsHistory;
    }

    public void setContributionList(List<ContributionEntity> contributionsList) {
        this.contributionList = contributionsList;
    }

    public void setLicenseHistory(String[] licenseHistory) {
        this.licenseHistory = licenseHistory;
    }

    public void setLicensePaymentHistory(List<LicensePaymentHistoryEntity> licensePaymentHistory) {
        this.licensePaymentHistory = licensePaymentHistory;
    }

    public void setPatentFirstRecord(Boolean patentFirstRecord) {
        this.patentFirstRecord = patentFirstRecord;
    }

    public void setPatentDay(LocalDate[] patentDay) {
        this.patentDay = patentDay;
    }

    public void setPistolCounter(Integer pistolCounter) {
        this.pistolCounter = pistolCounter;
    }

    public void setRifleCounter(Integer rifleCounter) {
        this.rifleCounter = rifleCounter;
    }

    public void setShotgunCounter(Integer shotgunCounter) {
        this.shotgunCounter = shotgunCounter;
    }

    public void setCompetitionHistory(List<CompetitionHistoryEntity> competitionHistory) {
        this.competitionHistory = competitionHistory;
    }

    public void setJudgingHistory(List<JudgingHistoryEntity> judgingHistory) {
        this.judgingHistory = judgingHistory;
    }
}
