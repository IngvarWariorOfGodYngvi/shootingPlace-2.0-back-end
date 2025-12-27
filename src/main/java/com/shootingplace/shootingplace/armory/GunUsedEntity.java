package com.shootingplace.shootingplace.armory;

import com.shootingplace.shootingplace.armory.gunRepresentation.GunRepresentationEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GunUsedEntity {

    @Id
    @UuidGenerator
    private String uuid;
    private String gunUUID;
    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private GunRepresentationEntity gunRepresentationEntity;
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
