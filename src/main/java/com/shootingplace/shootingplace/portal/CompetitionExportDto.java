package com.shootingplace.shootingplace.portal;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Builder
@Getter
@Setter
public class CompetitionExportDto {

    private String id;
    private String name;
    private List<String> disciplines;

    private List<ResultExportDto> results;
}

