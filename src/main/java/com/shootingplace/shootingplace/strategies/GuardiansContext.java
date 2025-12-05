package com.shootingplace.shootingplace.strategies;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("guardians")
public class GuardiansContext implements ProfileContext{

    private final ContributionStrategy contributionStrategy;
    private final EmailStrategy emailStrategy;
    private final SystemPropertiesStrategy systemPropertiesStrategy;

    @Autowired
    public GuardiansContext(GuardiansStrategy strategy) {
        this.contributionStrategy = strategy;
        this.emailStrategy = strategy;
        this.systemPropertiesStrategy = strategy;
    }

    @Override
    public ContributionStrategy getContributionStrategy() {
        return contributionStrategy;
    }

    @Override
    public EmailStrategy getEmailStrategy() {
        return emailStrategy;
    }

    @Override
    public SystemPropertiesStrategy getSystemPropertiesStrategy() {
        return systemPropertiesStrategy;
    }
}
