package com.shootingplace.shootingplace.ammoEvidence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AmmoEvidenceDTO {

    private String uuid;

    private LocalDate date;

    private String number;

    private List<AmmoInEvidenceDTO> ammoInEvidenceDTOList;

    private boolean open;

    private boolean forceOpen;
    private boolean locked;

}
