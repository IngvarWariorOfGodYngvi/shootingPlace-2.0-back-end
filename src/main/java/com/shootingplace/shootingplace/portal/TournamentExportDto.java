package com.shootingplace.shootingplace.portal;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Builder
@Getter
@Setter
public class TournamentExportDto {

    private String id;
    private String name;
    private String date;
    private String location;
    private String club;
    private String mainArbiter;
    private String rtsArbiter;

    private List<CompetitionExportDto> competitions;
}

