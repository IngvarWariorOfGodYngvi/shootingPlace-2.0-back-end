package com.shootingplace.shootingplace.strategies;

import com.shootingplace.shootingplace.contributions.ContributionEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@Profile("rcs")
public class PanaszewStrategy implements ContributionStrategy, EmailStrategy, SystemPropertiesStrategy {
    @Override
    public LocalDate calculateValidThru(LocalDate paymentDay, List<ContributionEntity> list) {
        LocalDate localDate = list.isEmpty() ? paymentDay.plusYears(1) : list.get(0).getValidThru().plusYears(1);
        logToCalculateValidThru(localDate);
        return localDate;
    }

    @Override
    public int getContributionCount(int baseCount) {
        logToContribution(baseCount);
        return 1;
    }

    @Override
    public LocalDate calculateFirstValidThru(LocalDate paymentDay) {
        LocalDate localDate = paymentDay.plusYears(1);
        logToCalculateValidThru(localDate);
        return localDate;
    }

    @Override
    public String getBcc() {
        String email = "biuro@rcspanaszew.pl";
        logBcc(email);
        return email;
    }

    @Override
    public String getDatesString() {
        return "<p>poniedziałek - piątek 16:00 - 20:00<br />sobota - niedziela - 10:00 – 20:00</p>\n";
    }

    @Override
    public String getAHrefSite() {
        return "<a href=\"https://www.rcspanaszew.pl\">www.rcspanaszew.pl</a>";
    }
}
