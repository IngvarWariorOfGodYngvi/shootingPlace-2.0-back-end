package com.shootingplace.shootingplace.strategies;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class TestStrategy implements ContributionStrategy, EmailStrategy, SystemPropertiesStrategy {
    @Override
    public String getDatesString() {
        return "<p>poniedziałek 13:00 - 20:00<br />wtorek 16:30 - 20:00<br />środa 13:00 - 20:00<br />czwartek - nieczynne<br />piątek 16:30 - 20:00<br />sobota - nieczynne<br />niedziela - nieczynne</p>\n";
    }

    @Override
    public String getAhrefSite() {
        return "<a href=\"https://www.smartstrzelnica.pl\">www.smartstrzelnica.pl</a>";

    }
}
