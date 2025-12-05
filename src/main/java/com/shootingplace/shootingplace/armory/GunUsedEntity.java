package com.shootingplace.shootingplace.armory;

import com.shootingplace.shootingplace.armory.gunRepresentation.GunRepresentationEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GunUsedEntity {

    @Id
    @GeneratedValue
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

    public String getUuid() {
        return uuid;
    }

    public String getGunUUID() {
        return gunUUID;
    }

    public void setGunUUID(String gunUUID) {
        this.gunUUID = gunUUID;
    }

    public LocalDate getUsedDate() {
        return usedDate;
    }


    public LocalTime getUsedTime() {
        return usedTime;
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

    public void setAcceptanceDate(LocalDate acceptanceDate) {
        this.acceptanceDate = acceptanceDate;
    }

    public void setAcceptanceTime(LocalTime acceptanceTime) {
        this.acceptanceTime = acceptanceTime;
    }

    public String getIssuanceBy() {
        return issuanceBy;
    }

    public void setIssuanceBy(String issuanceBy) {
        this.issuanceBy = issuanceBy;
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

    public void setGunTakerName(String gunTakerName) {
        this.gunTakerName = gunTakerName;
    }

    public String getIssuanceSign() {
        return issuanceSign;
    }

    public void setIssuanceSign(String issuanceSign) {
        this.issuanceSign = issuanceSign;
    }

    public void setUsedDate(LocalDate usedDate) {
        this.usedDate = usedDate;
    }

    public void setUsedTime(LocalTime usedTime) {
        this.usedTime = usedTime;
    }

    public String getGunReturnerName() {
        return gunReturnerName;
    }

    public void setGunReturnerName(String returnerName) {
        this.gunReturnerName = returnerName;
    }

    public String getGunReturnerSign() {
        return gunReturnerSign;
    }

    public void setGunReturnerSign(String returnerSign) {
        this.gunReturnerSign = returnerSign;
    }

    public LocalDate getAcceptanceDate() {
        return acceptanceDate;
    }

    public LocalTime getAcceptanceTime() {
        return acceptanceTime;
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

    public GunRepresentationEntity getGunRepresentationEntity() {
        return gunRepresentationEntity;
    }

    public void setGunRepresentationEntity(GunRepresentationEntity gunRepresentationEntity) {
        this.gunRepresentationEntity = gunRepresentationEntity;
    }
}
