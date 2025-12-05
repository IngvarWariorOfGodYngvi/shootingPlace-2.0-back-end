package com.shootingplace.shootingplace.bookOfRegistrationOfStayAtTheShootingPlace;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
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
public class RegistrationRecordEntity {

    @Id
    @GeneratedValue
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


    public String getUuid() {
        return uuid;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public int getDayIndex() {
        return dayIndex;
    }

    public void setDayIndex(int dayIndex) {
        this.dayIndex = dayIndex;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getWeaponPermission() {
        return weaponPermission;
    }

    public void setWeaponPermission(String weaponPermission) {
        this.weaponPermission = weaponPermission;
    }

    public boolean isStatementOnReadingTheShootingPlaceRegulations() {
        return statementOnReadingTheShootingPlaceRegulations;
    }

    public void setStatementOnReadingTheShootingPlaceRegulations(boolean statementOnReadingTheShootingPlaceRegulations) {
        this.statementOnReadingTheShootingPlaceRegulations = statementOnReadingTheShootingPlaceRegulations;
    }

    public boolean isDataProcessingAgreement() {
        return dataProcessingAgreement;
    }

    public void setDataProcessingAgreement(boolean dataProcessingAgreement) {
        this.dataProcessingAgreement = dataProcessingAgreement;
    }

    public String getImageUUID() {
        return imageUUID;
    }

    public void setImageUUID(String imageUUID) {
        this.imageUUID = imageUUID;
    }

    public String getPeselOrID() {
        return peselOrID;
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
