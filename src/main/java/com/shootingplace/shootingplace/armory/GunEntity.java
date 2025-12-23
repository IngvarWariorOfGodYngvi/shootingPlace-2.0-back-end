package com.shootingplace.shootingplace.armory;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.List;

@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GunEntity {

    @Id
    @UuidGenerator
    private String uuid;
    @NotNull
    private String modelName;
    @NotNull
    private String caliber;
    @NotNull
    private String serialNumber;

    private String productionYear;
    @NotNull
    private String gunType;
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("usedDate DESC")
    private List<GunUsedEntity> gunUsedList;

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

    public void setImgUUID(String imgUUID) {
        this.imgUUID = imgUUID;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public void setCaliber(String caliber) {
        this.caliber = caliber;
    }

    public void setGunType(String gunType) {
        this.gunType = gunType;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public void setProductionYear(String productionYear) {
        this.productionYear = productionYear;
    }


    public void setNumberOfMagazines(String numberOfMagazines) {
        this.numberOfMagazines = numberOfMagazines;
    }

    public void setGunCertificateSerialNumber(String gunCertificateSerialNumber) {
        this.gunCertificateSerialNumber = gunCertificateSerialNumber;
    }

    public void setAdditionalEquipment(String additionalEquipment) {
        this.additionalEquipment = additionalEquipment;
    }

    public void setRecordInEvidenceBook(String recordInEvidenceBook) {
        this.recordInEvidenceBook = recordInEvidenceBook;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setInStock(boolean inStock) {
        this.inStock = inStock;
    }

    public void setInUseStatus(String inUseStatus) {
        this.inUseStatus = inUseStatus;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public void setBasisForPurchaseOrAssignment(String basisForPurchaseOrAssignment) {
        this.basisForPurchaseOrAssignment = basisForPurchaseOrAssignment;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public void setGunUsedList(List<GunUsedEntity> gunUsedList) {
        this.gunUsedList = gunUsedList;
    }

    public void setAddedDate(LocalDate addedDate) {
        this.addedDate = addedDate;
    }

    public void setAddedSign(String addedSign) {
        this.addedSign = addedSign;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

    public void setAddedUserUUID(String addedUserUUID) {
        this.addedUserUUID = addedUserUUID;
    }

    public void setRemovedBy(String removedBy) {
        this.removedBy = removedBy;
    }

    public void setRemovedSign(String removedSign) {
        this.removedSign = removedSign;
    }

    public void setRemovedUserUUID(String userUUID) {
        this.removedUserUUID = userUUID;
    }

    public void setRemovedDate(LocalDate removedDate) {
        this.removedDate = removedDate;
    }

    public void setBasisOfRemoved(String basisOfRemoved) {
        this.basisOfRemoved = basisOfRemoved;
    }
}
