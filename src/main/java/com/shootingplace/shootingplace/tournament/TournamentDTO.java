package com.shootingplace.shootingplace.tournament;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TournamentDTO {

    private String name;
    private LocalDate date;
    private String tournamentUUID;
    private boolean open;
    private boolean wzss;
    private List<String> shootingAxis;

}
