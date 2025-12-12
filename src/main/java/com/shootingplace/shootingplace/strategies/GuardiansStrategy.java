package com.shootingplace.shootingplace.strategies;

import com.shootingplace.shootingplace.contributions.ContributionEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

@Component
@Profile("guardians")
public class GuardiansStrategy implements ContributionStrategy, EmailStrategy, SystemPropertiesStrategy {

    @Override
    public LocalDate calculateValidThru(LocalDate paymentDay, List<ContributionEntity> list) {

        LocalDate validThru;

        if (list.isEmpty()) {
            validThru = calculateFirstYearValidThru(paymentDay);
        } else {
            validThru = list.get(0)
                    .getValidThru()
                    .plusYears(1);
        }

        logToCalculateValidThru(validThru);
        return validThru;
    }

    @Override
    public LocalDate calculateFirstValidThru(LocalDate paymentDay) {
        LocalDate validThru = calculateFirstYearValidThru(paymentDay);
        logToCalculateValidThru(validThru);
        return validThru;
    }

    @Override
    public int getContributionCount(int baseCount) {
        logToContribution(baseCount);
        return 1;
    }
    private LocalDate calculateFirstYearValidThru(LocalDate paymentDay) {

        int year = paymentDay.getMonthValue() >= Month.NOVEMBER.getValue()
                ? paymentDay.getYear() + 1
                : paymentDay.getYear();

        return LocalDate.of(year, 12, 31);
    }

    @Override
    public String getBcc() {
        String email = "biuro@ksguardians.pl";
        logBcc(email);
        return email;
    }

    @Override
    public String getDatesString() {
        return "<p>poniedziałek 13:00 - 20:00<br />" +
                "wtorek 16:30 - 20:00<br />" +
                "środa 13:00 - 20:00<br />" +
                "czwartek - nieczynne<br />" +
                "piątek 16:30 - 20:00<br />" +
                "sobota - nieczynne<br />" +
                "niedziela - nieczynne</p>\n";
    }

    @Override
    public String getAHrefSite() {
        return "<a href=\"https://www.ksguardians.pl/\">www.ksguardians.pl</a>";
    }
}