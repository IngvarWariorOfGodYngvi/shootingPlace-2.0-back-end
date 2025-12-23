package com.shootingplace.shootingplace.armory;

import com.shootingplace.shootingplace.armory.gunRepresentation.GunRepresentationEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
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

    public void setGunUUID(String gunUUID) {
        this.gunUUID = gunUUID;
    }


    public void setIssuanceDate(LocalDate issuanceDate) {
        this.issuanceDate = issuanceDate;
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

    public void setIssuanceBy(String issuanceBy) {
        this.issuanceBy = issuanceBy;
    }

    public void setGunTakerSign(String gunTakerSign) {
        this.gunTakerSign = gunTakerSign;
    }

    public void setGunTakerName(String gunTakerName) {
        this.gunTakerName = gunTakerName;
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

    public void setGunReturnerName(String returnerName) {
        this.gunReturnerName = returnerName;
    }

    public void setGunReturnerSign(String returnerSign) {
        this.gunReturnerSign = returnerSign;
    }

    public void setAcceptanceBy(String acceptanceBy) {
        this.acceptanceBy = acceptanceBy;
    }

    public void setAcceptanceSign(String acceptanceSign) {
        this.acceptanceSign = acceptanceSign;
    }

    public void setAdnotation(String adnotation) {
        this.adnotation = adnotation;
    }

    public void setGunRepresentationEntity(GunRepresentationEntity gunRepresentationEntity) {
        this.gunRepresentationEntity = gunRepresentationEntity;
    }
}
