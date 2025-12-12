package com.shootingplace.shootingplace.strategies;

import com.shootingplace.shootingplace.contributions.ContributionEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
@Component
@Profile("uks")
public class MechanikStrategy implements ContributionStrategy,EmailStrategy,SystemPropertiesStrategy {
    @Override
    public LocalDate calculateValidThru(LocalDate paymentDay, List<ContributionEntity> list) {
        LocalDate localDate = list.isEmpty() ? paymentDay.plusMonths(6) : list.get(0).getValidThru().plusYears(1);
        logToCalculateValidThru(localDate);
        return localDate;
    }

    @Override
    public LocalDate calculateFirstValidThru(LocalDate paymentDay) {
        LocalDate localDate = paymentDay.plusMonths(6);
        logToCalculateValidThru(localDate);
        return localDate;
    }

    @Override
    public String getBcc() {
        String email = "biuro@uksmechanik.com";
        logBcc(email);
        return email;
    }

    @Override
    public String getAHrefSite() {
        return "<a href=\"https://www.uksmechanik.com\">www.uksmechanik.com</a>";
    }
}
