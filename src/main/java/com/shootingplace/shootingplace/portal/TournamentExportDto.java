package com.shootingplace.shootingplace.portal;

import java.time.LocalDate;
import java.util.List;

public class TournamentExportDto {

    private String id;
    private String name;
    private LocalDate date;
    private String location;

    private List<CompetitionExportDto> competitions;
}

