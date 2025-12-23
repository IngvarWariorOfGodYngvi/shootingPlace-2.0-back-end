package com.shootingplace.shootingplace.armory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Gun {

    private String uuid;
    private String modelName;
    private String caliber;
    private String serialNumber;

    private String productionYear;
    private String gunType;

    private String numberOfMagazines;
    private String gunCertificateSerialNumber;

    private String additionalEquipment;
    private String recordInEvidenceBook;

    private String basisForPurchaseOrAssignment;

    private String comment;

    private boolean inStock;
    private boolean available;
    private String imgUUID;

    private String barcode;

    private LocalDate addedDate;
    private String addedSing;
    private String addedBy;
    private String addedUserUUID;
    private String removedBy;
    private String removedSing;
    private String removedUserUUID;
    private LocalDate removedDate;
    private String basisOfRemoved;

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public void setCaliber(String caliber) {
        this.caliber = caliber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public void setProductionYear(String productionYear) {
        this.productionYear = productionYear;
    }

    public void setGunType(String gunType) {
        this.gunType = gunType;
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

    public void setBasisForPurchaseOrAssignment(String basisForPurchaseOrAssignment) {
        this.basisForPurchaseOrAssignment = basisForPurchaseOrAssignment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public void setInStock(boolean inStock) {
        this.inStock = inStock;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public void setImgUUID(String imgUUID) {
        this.imgUUID = imgUUID;
    }

    public void setAddedDate(LocalDate addedDate) {
        this.addedDate = addedDate;
    }

    public void setAddedSing(String addedSing) {
        this.addedSing = addedSing;
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

    public void setRemovedSign(String removedSing) {
        this.removedSing = removedSing;
    }

    public void setRemovedUserUUID(String removedUserUUID) {
        this.removedUserUUID = removedUserUUID;
    }

    public void setRemovedDate(LocalDate removedDate) {
        this.removedDate = removedDate;
    }

    public void setBasisOfRemoved(String basisOfRemoved) {
        this.basisOfRemoved = basisOfRemoved;
    }

    public String getFullName() {
        return this.modelName + " " + this.serialNumber;
    }
}
