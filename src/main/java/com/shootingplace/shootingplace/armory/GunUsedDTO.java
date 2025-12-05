package com.shootingplace.shootingplace.armory;

import com.shootingplace.shootingplace.armory.gunRepresentation.GunRepresentationEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

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

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Gun getGun() {
        return gun;
    }

    public void setGun(Gun gun) {
        this.gun = gun;
    }

    public LocalDate getUsedDate() {
        return usedDate;
    }

    public void setUsedDate(LocalDate usedDate) {
        this.usedDate = usedDate;
    }

    public LocalTime getUsedTime() {
        return usedTime;
    }

    public void setUsedTime(LocalTime usedTime) {
        this.usedTime = usedTime;
    }

    public LocalDate getIssuanceDate() {
        return issuanceDate;
    }

    public void setIssuanceDate(LocalDate issuanceDate) {
        this.issuanceDate = issuanceDate;
    }

    public LocalTime getIssuanceTime() {
        return issuanceTime;
    }

    public void setIssuanceTime(LocalTime issuanceTime) {
        this.issuanceTime = issuanceTime;
    }

    public String getIssuanceBy() {
        return issuanceBy;
    }

    public void setIssuanceBy(String issuanceBy) {
        this.issuanceBy = issuanceBy;
    }

    public String getIssuanceSign() {
        return issuanceSign;
    }

    public void setIssuanceSign(String issuanceSign) {
        this.issuanceSign = issuanceSign;
    }

    public String getGunTakerSign() {
        return gunTakerSign;
    }

    public void setGunTakerSign(String gunTakerSign) {
        this.gunTakerSign = gunTakerSign;
    }

    public String getGunTakerName() {
        return gunTakerName;
    }

    public String getGunReturnerName() {
        return gunReturnerName;
    }

    public void setGunReturnerName(String gunReturnerName) {
        this.gunReturnerName = gunReturnerName;
    }

    public String getGunReturnerSign() {
        return gunReturnerSign;
    }

    public void setGunReturnerSign(String gunReturnerSign) {
        this.gunReturnerSign = gunReturnerSign;
    }

    public void setGunTakerName(String gunTakerName) {
        this.gunTakerName = gunTakerName;

    }

    public LocalDate getAcceptanceDate() {
        return acceptanceDate;
    }

    public void setAcceptanceDate(LocalDate acceptanceDate) {
        this.acceptanceDate = acceptanceDate;
    }

    public LocalTime getAcceptanceTime() {
        return acceptanceTime;
    }

    public void setAcceptanceTime(LocalTime acceptanceTime) {
        this.acceptanceTime = acceptanceTime;
    }

    public String getAcceptanceBy() {
        return acceptanceBy;
    }

    public void setAcceptanceBy(String acceptanceBy) {
        this.acceptanceBy = acceptanceBy;
    }

    public String getAcceptanceSign() {
        return acceptanceSign;
    }

    public void setAcceptanceSign(String acceptanceSign) {
        this.acceptanceSign = acceptanceSign;
    }

    public String getAdnotation() {
        return adnotation;
    }

    public void setAdnotation(String adnotation) {
        this.adnotation = adnotation;
    }

    public GunRepresentationEntity getGunRepresentation() {
        return gunRepresentation;
    }

    public void setGunRepresentation(GunRepresentationEntity gunRepresentation) {
        this.gunRepresentation = gunRepresentation;
    }
}
