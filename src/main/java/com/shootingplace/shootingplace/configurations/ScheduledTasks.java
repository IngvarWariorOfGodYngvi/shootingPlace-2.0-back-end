package com.shootingplace.shootingplace.configurations;

import com.shootingplace.shootingplace.ammoEvidence.AmmoEvidenceService;
import com.shootingplace.shootingplace.armory.AmmoUsedService;
import com.shootingplace.shootingplace.bookOfRegistrationOfStayAtTheShootingPlace.RegistrationRecordsService;
import com.shootingplace.shootingplace.email.EmailService;
import com.shootingplace.shootingplace.enums.ProfilesEnum;
import com.shootingplace.shootingplace.member.MemberService;
import com.shootingplace.shootingplace.workingTimeEvidence.WorkingTimeEvidenceService;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ScheduledTasks {

    private final Environment environment;

    private final WorkingTimeEvidenceService workingTimeEvidenceService;
    private final MemberService memberService;
    private final AmmoEvidenceService ammoEvidenceService;
    private final AmmoUsedService ammoUsedService;
    private final RegistrationRecordsService registrationRecordsService;
    private final EmailService emailService;

    public ScheduledTasks(Environment environment, WorkingTimeEvidenceService workRepo,
                          MemberService memberService,
                          AmmoEvidenceService ammoEvidenceService,
                          AmmoUsedService ammoUsedService,
                          RegistrationRecordsService registrationRecordsService,
                          EmailService emailService) {
        this.environment = environment;
        this.workingTimeEvidenceService = workRepo;
        this.memberService = memberService;
        this.ammoEvidenceService = ammoEvidenceService;
        this.ammoUsedService = ammoUsedService;
        this.registrationRecordsService = registrationRecordsService;
        this.emailService = emailService;
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void recountAmmo() {
        ammoUsedService.recountAmmo();
    }

    @Transactional
    @Scheduled(cron = "0 1 23 * * *")
    public void closeOpenedAmmoList() {
        ammoEvidenceService.automationCloseEvidence();
    }

    @Transactional
    @Scheduled(cron = "0 0 22-23,0-6 * * *")
    public void sendAllWorkersGoHome() {
        workingTimeEvidenceService.closeAllActiveWorkTime();
    }

    @Transactional
    @Scheduled(cron = "0 0 13 * * *")
    public void checkMembers() {
        memberService.checkMembers();
    }

    @Transactional
    @Scheduled(cron = "0 30 11 ? * *")
    public void setEndTimeToAllRegistrationRecordEntity() {
        registrationRecordsService.setEndTimeToAllRegistrationRecordEntity();
    }

    @Transactional
    @Scheduled(cron = "0 0 15 * * *")
    public void sendRemindersForNonActive() {
        if (!environment.getActiveProfiles()[0].equals(ProfilesEnum.TEST.getName())) {
            emailService.sendRemindersForNonActive();
        }
    }

    @Transactional
    @Scheduled(cron = "0 0 15 * * *")
    public void sendRemindersForActiveOneMonthBefore() {
        if (!environment.getActiveProfiles()[0].equals(ProfilesEnum.TEST.getName())) {
            emailService.sendRemindersForActiveOneMonthBefore();
        }
    }
    @Transactional
    @Scheduled(cron = "0 0 13 * * *")
    public void sendCongratulationsOnTheAnniversary() {
        if (!environment.getActiveProfiles()[0].equals(ProfilesEnum.TEST.getName())) {
            emailService.sendCongratulationsOnTheAnniversary();
        }
    }

}
