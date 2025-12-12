package com.shootingplace.shootingplace.strategies;

import com.shootingplace.shootingplace.contributions.ContributionEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.util.List;

public interface ContributionStrategy {
    // all default for prod
    Logger LOG = LogManager.getLogger(ContributionStrategy.class);

    default LocalDate calculateValidThru(LocalDate paymentDay, List<ContributionEntity> list) {
        LocalDate localDate = list.isEmpty() ? paymentDay.plusMonths(3) : list.get(0).getValidThru().plusMonths(3);
        logToCalculateValidThru(localDate);
        return localDate;
    }
    default LocalDate calculateFirstValidThru(LocalDate paymentDay) {
        LocalDate localDate = paymentDay.plusMonths(3);
        logToCalculateValidThru(localDate);
        return localDate;
    }

    default int getContributionCount(int baseCount) {
        logToContribution(baseCount);
        return baseCount;
    }

    default void logToContribution(int baseCount) {
        LOG.info("ilość składek : " + baseCount);
    }
    default void logToCalculateValidThru(LocalDate date) {
        LOG.info("ważność składki : " + date);
    }

}
