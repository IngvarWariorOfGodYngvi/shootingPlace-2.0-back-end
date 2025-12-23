package com.shootingplace.shootingplace.history;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JudgingHistoryDTO {

    private String uuid;
    private String firstName;
    private String secondName;
    private String tournamentName;
    private String tournamentUUID;
    private String judgingFunction;
    private LocalDate date;
    private LocalTime time;

}
