package com.shootingplace.shootingplace.tournament;

import lombok.*;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TournamentDTO {

    private String name;
    private LocalDate date;
    private String tournamentUUID;
    private boolean ranking;
    private boolean dynamic;

}
