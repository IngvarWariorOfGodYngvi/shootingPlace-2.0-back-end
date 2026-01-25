package com.shootingplace.shootingplace.portal;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PortalExportRequestDto {
    private List<TournamentExportDto> tournaments;
}


