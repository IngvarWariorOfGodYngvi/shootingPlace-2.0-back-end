package com.shootingplace.shootingplace.armory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getCaliber() {
        return caliber;
    }

    public void setCaliber(String caliber) {
        this.caliber = caliber;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getProductionYear() {
        return productionYear;
    }

    public void setProductionYear(String productionYear) {
        this.productionYear = productionYear;
    }

    public String getGunType() {
        return gunType;
    }

    public void setGunType(String gunType) {
        this.gunType = gunType;
    }

    public String getNumberOfMagazines() {
        return numberOfMagazines;
    }

    public void setNumberOfMagazines(String numberOfMagazines) {
        this.numberOfMagazines = numberOfMagazines;
    }

    public String getGunCertificateSerialNumber() {
        return gunCertificateSerialNumber;
    }

    public void setGunCertificateSerialNumber(String gunCertificateSerialNumber) {
        this.gunCertificateSerialNumber = gunCertificateSerialNumber;
    }

    public String getAdditionalEquipment() {
        return additionalEquipment;
    }

    public void setAdditionalEquipment(String additionalEquipment) {
        this.additionalEquipment = additionalEquipment;
    }

    public String getRecordInEvidenceBook() {
        return recordInEvidenceBook;
    }

    public void setRecordInEvidenceBook(String recordInEvidenceBook) {
        this.recordInEvidenceBook = recordInEvidenceBook;
    }

    public String getBasisForPurchaseOrAssignment() {
        return basisForPurchaseOrAssignment;
    }

    public void setBasisForPurchaseOrAssignment(String basisForPurchaseOrAssignment) {
        this.basisForPurchaseOrAssignment = basisForPurchaseOrAssignment;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isInStock() {
        return inStock;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public void setInStock(boolean inStock) {
        this.inStock = inStock;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getImgUUID() {
        return imgUUID;
    }

    public void setImgUUID(String imgUUID) {
        this.imgUUID = imgUUID;
    }

    public LocalDate getAddedDate() {
        return addedDate;
    }

    public void setAddedDate(LocalDate addedDate) {
        this.addedDate = addedDate;
    }

    public void setAddedSing(String addedSing) {
        this.addedSing = addedSing;
    }

    public String getAddedSing() {
        return addedSing;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedUserUUID(String addedUserUUID) {
        this.addedUserUUID = addedUserUUID;
    }

    public String getAddedUserUUID() {
        return addedUserUUID;
    }

    public void setRemovedBy(String removedBy) {
        this.removedBy = removedBy;
    }

    public String getRemovedBy() {
        return removedBy;
    }

    public void setRemovedSign(String removedSing) {
        this.removedSing = removedSing;
    }

    public String getRemovedSing() {
        return removedSing;
    }

    public void setRemovedUserUUID(String removedUserUUID) {
        this.removedUserUUID = removedUserUUID;
    }

    public String getRemovedUserUUID() {
        return removedUserUUID;
    }

    public void setRemovedDate(LocalDate removedDate) {
        this.removedDate = removedDate;
    }

    public LocalDate getRemovedDate() {
        return removedDate;
    }

    public void setBasisOfRemoved(String basisOfRemoved) {
        this.basisOfRemoved = basisOfRemoved;
    }

    public String getBasisOfRemoved() {
        return basisOfRemoved;
    }

    public String getFullName() {
        return this.modelName + " " + this.serialNumber;
    }
}
