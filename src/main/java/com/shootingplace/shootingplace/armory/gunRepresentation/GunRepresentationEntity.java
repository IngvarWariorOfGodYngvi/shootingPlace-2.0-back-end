package com.shootingplace.shootingplace.armory.gunRepresentation;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GunRepresentationEntity {
    @Id
    @UuidGenerator
    private String uuid;
    private String gunUUID;
    @NotNull
    private String modelName;
    @NotNull
    private String caliber;
    @NotNull
    private String serialNumber;

    private String productionYear;
    @NotNull
    private String gunType;

    private String numberOfMagazines;
    private String gunCertificateSerialNumber;

    private String additionalEquipment;
    private String recordInEvidenceBook;

    private String basisForPurchaseOrAssignment;

    private String comment;
    private boolean inStock;
    private boolean available;
    private String inUseStatus;
    private String imgUUID;
    private String barcode;

    private LocalDate addedDate;
    private String addedSign;
    private String addedBy;
    private String addedUserUUID;
    private String removedBy;
    private String removedSign;
    private String removedUserUUID;
    private LocalDate removedDate;
    private String basisOfRemoved;
}
