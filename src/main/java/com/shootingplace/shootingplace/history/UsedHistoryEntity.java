package com.shootingplace.shootingplace.history;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;

@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UsedHistoryEntity {

    @Id
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


    public void setGunName(String gunName) {
        this.gunName = gunName;
    }

    public void setGunUUID(String gunUUID) {
        this.gunUUID = gunUUID;
    }

    public void setGunSerialNumber(String gunSerialNumber) {
        this.gunSerialNumber = gunSerialNumber;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public void setUsedType(String usedType) {
        this.usedType = usedType;
    }

    public void setEvidenceUUID(String evidenceUUID) {
        this.evidenceUUID = evidenceUUID;
    }

    public void setReturnToStore(boolean returnToStore) {
        this.returnToStore = returnToStore;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setMemberUUID(String userUUID) {
        this.memberUUID = userUUID;
    }
}
