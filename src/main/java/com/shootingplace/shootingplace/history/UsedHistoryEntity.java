package com.shootingplace.shootingplace.history;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UsedHistoryEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private String uuid;

    private String gunName;
    private String gunUUID;
    private String gunSerialNumber;
    private LocalDateTime dateTime;

    private String usedType;
    private String evidenceUUID;
    private boolean returnToStore;
    private String userName;
    private String memberUUID;


    public String getUuid() {
        return uuid;
    }

    public String getGunName() {
        return gunName;
    }

    public void setGunName(String gunName) {
        this.gunName = gunName;
    }

    public String getGunUUID() {
        return gunUUID;
    }

    public void setGunUUID(String gunUUID) {
        this.gunUUID = gunUUID;
    }

    public String getGunSerialNumber() {
        return gunSerialNumber;
    }

    public void setGunSerialNumber(String gunSerialNumber) {
        this.gunSerialNumber = gunSerialNumber;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getUsedType() {
        return usedType;
    }

    public void setUsedType(String usedType) {
        this.usedType = usedType;
    }

    public String getEvidenceUUID() {
        return evidenceUUID;
    }

    public void setEvidenceUUID(String evidenceUUID) {
        this.evidenceUUID = evidenceUUID;
    }

    public boolean isReturnToStore() {
        return returnToStore;
    }

    public void setReturnToStore(boolean returnToStore) {
        this.returnToStore = returnToStore;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMemberUUID() {
        return memberUUID;
    }

    public void setMemberUUID(String userUUID) {
        this.memberUUID = userUUID;
    }
}
