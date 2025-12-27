package com.shootingplace.shootingplace.armory;

import com.shootingplace.shootingplace.armory.gunRepresentation.GunRepresentationEntity;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GunUsedDTO {

    private String uuid;
    private Gun gun;
    private GunRepresentationEntity gunRepresentation;
    private LocalDate usedDate;
    private LocalTime usedTime;
    private LocalDate issuanceDate;
    private LocalTime issuanceTime;
    private String issuanceBy;
    private String issuanceSign;
    private String gunTakerSign;
    private String gunTakerName;
    private String gunReturnerName;
    private String gunReturnerSign;
    private LocalDate acceptanceDate;
    private LocalTime acceptanceTime;
    private String adnotation;
    private String acceptanceBy;
    private String acceptanceSign;

}
