package com.shootingplace.shootingplace.statistics;

import java.util.Map;

public record MembersStatsDTO(
        long adultAll,
        long adultActive,
        long adultInactive,

        long childAll,
        long childActive,
        long childInactive,

        long adultErased,
        long childErased,

        long club1PzssActiveLicense,
        long club1PzssInactiveLicense,

        long unpaidInPzssPortal,
        long newLicensesThisYear,
        Map<String, Long> membersByGroup
) {}

