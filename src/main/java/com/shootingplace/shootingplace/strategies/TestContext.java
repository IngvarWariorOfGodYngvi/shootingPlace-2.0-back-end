package com.shootingplace.shootingplace.strategies;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class TestContext implements ProfileContext {
    private final ContributionStrategy contributionStrategy;
    private final EmailStrategy emailStrategy;
    private final SystemPropertiesStrategy systemPropertiesStrategy;

    @Autowired
    public TestContext(TestStrategy strategy, SystemPropertiesStrategy systemPropertiesStrategy) {
        this.contributionStrategy = strategy;
        this.emailStrategy = strategy;
        this.systemPropertiesStrategy = systemPropertiesStrategy;
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
