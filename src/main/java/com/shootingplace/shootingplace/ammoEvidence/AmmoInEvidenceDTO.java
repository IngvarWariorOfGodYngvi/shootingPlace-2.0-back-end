package com.shootingplace.shootingplace.ammoEvidence;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AmmoInEvidenceDTO {

    private String uuid;

    private String caliberName;

    private Integer quantity;

    private List<AmmoUsedToEvidenceDTO> ammoUsedToEvidenceDTOList;

    private String signedBy;

    private String imageUUID;
    private LocalDate date;
    private LocalTime time;
    private boolean locked;
}
