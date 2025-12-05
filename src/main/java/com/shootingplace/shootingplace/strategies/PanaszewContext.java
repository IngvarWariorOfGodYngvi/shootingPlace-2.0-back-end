package com.shootingplace.shootingplace.strategies;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("rcs")
public class PanaszewContext implements ProfileContext {

    private final ContributionStrategy contributionStrategy;
    private final EmailStrategy emailStrategy;
    private final SystemPropertiesStrategy systemPropertiesStrategy;

    @Autowired
    public PanaszewContext(PanaszewStrategy strategy) {
        this.contributionStrategy = strategy;
        this.emailStrategy = strategy;
        this.systemPropertiesStrategy = strategy;
    }

    public ContributionStrategy getContributionStrategy() {
        return contributionStrategy;
    }

    public EmailStrategy getEmailStrategy() {
        return emailStrategy;
    }

    @Override
    public SystemPropertiesStrategy getSystemPropertiesStrategy() {
        return systemPropertiesStrategy;
    }
}
