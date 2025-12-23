package com.shootingplace.shootingplace.bookOfRegistrationOfStayAtTheShootingPlace;

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
public class RegistrationRecordEntity {

    @Id
    @UuidGenerator
    private String uuid;

    private LocalDateTime dateTime;
    private LocalDateTime endDateTime;
    private int dayIndex;
    private String firstName;
    private String secondName;
    private String peselOrID;

    private String address;
    private String weaponPermission;

    private boolean statementOnReadingTheShootingPlaceRegulations;
    private boolean dataProcessingAgreement;
    
    private String imageUUID;


    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public void setDayIndex(int dayIndex) {
        this.dayIndex = dayIndex;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setWeaponPermission(String weaponPermission) {
        this.weaponPermission = weaponPermission;
    }

    public void setStatementOnReadingTheShootingPlaceRegulations(boolean statementOnReadingTheShootingPlaceRegulations) {
        this.statementOnReadingTheShootingPlaceRegulations = statementOnReadingTheShootingPlaceRegulations;
    }

    public void setDataProcessingAgreement(boolean dataProcessingAgreement) {
        this.dataProcessingAgreement = dataProcessingAgreement;
    }

    public void setImageUUID(String imageUUID) {
        this.imageUUID = imageUUID;
    }

    public void setPeselOrID(String peselOrID) {
        this.peselOrID = peselOrID;
    }

    public String getNameOnRecord() {
        return this.secondName + " " + this.firstName;
    }

    @Override
    public String toString() {
        return "RegistrationRecordEntity{" +
                "uuid='" + uuid + '\'' +
                ", dateTime=" + dateTime +
                ", endDateTime=" + endDateTime +
                ", dayIndex=" + dayIndex +
                ", firstName='" + firstName + '\'' +
                ", secondName='" + secondName + '\'' +
                ", peselOrID='" + peselOrID + '\'' +
                ", address='" + address + '\'' +
                ", weaponPermission='" + weaponPermission + '\'' +
                ", statementOnReadingTheShootingPlaceRegulations=" + statementOnReadingTheShootingPlaceRegulations +
                ", dataProcessingAgreement=" + dataProcessingAgreement +
                ", imageUUID='" + imageUUID + '\'' +
                '}';
    }
}
