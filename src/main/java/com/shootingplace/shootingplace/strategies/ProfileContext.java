package com.shootingplace.shootingplace.strategies;

public interface ProfileContext {
    ContributionStrategy getContributionStrategy();
    EmailStrategy getEmailStrategy();
    SystemPropertiesStrategy getSystemPropertiesStrategy();
}


