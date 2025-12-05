package com.shootingplace.shootingplace.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PersonalStatistics {

    int contributionCounter;
    int monthsFromFirstVisit;
    int visitCounter;
    MemberAmmo ammo;
    int competitionHistoryCounter;

    @Override
    public String toString() {
        return  "contributionCounter= " + contributionCounter +
                "\n monthsFromFirstVisit= " + monthsFromFirstVisit +
                "\n visitCounter= " + visitCounter +
                "\n ammo= " + ammo +
                "\n competitionHistoryCounter= " + competitionHistoryCounter;
    }
}
