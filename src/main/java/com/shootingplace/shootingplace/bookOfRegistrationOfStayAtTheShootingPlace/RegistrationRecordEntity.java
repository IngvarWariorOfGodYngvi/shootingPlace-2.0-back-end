package com.shootingplace.shootingplace.bookOfRegistrationOfStayAtTheShootingPlace;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;

@Getter @Setter
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
