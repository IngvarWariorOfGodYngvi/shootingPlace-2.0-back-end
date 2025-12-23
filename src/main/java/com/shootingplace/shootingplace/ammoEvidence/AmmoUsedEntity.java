package com.shootingplace.shootingplace.ammoEvidence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AmmoUsedEntity {

    @Id
    @UuidGenerator
    private String uuid;

    private String caliberName;

    private String caliberUUID;

    private String memberUUID;

    private Integer otherPersonEntityID;

    private String userName;

    private Integer counter;

    private LocalDate date;
    private LocalTime time;

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setCaliberName(String caliberName) {
        this.caliberName = caliberName;
    }

    public void setMemberUUID(String memberUUID) {
        this.memberUUID = memberUUID;
    }

    public void setCaliberUUID(String caliberUUID) {
        this.caliberUUID = caliberUUID;
    }

    public void setCounter(Integer counter) {
        this.counter = counter;
    }

    public void setOtherPersonEntityID(Integer otherPersonEntityID) {
        this.otherPersonEntityID = otherPersonEntityID;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        return "AmmoUsedEntity{" +
                "uuid='" + uuid + '\'' +
                ", caliberName='" + caliberName + '\'' +
                ", caliberUUID='" + caliberUUID + '\'' +
                ", memberUUID='" + memberUUID + '\'' +
                ", otherPersonEntityID=" + otherPersonEntityID +
                ", userName='" + userName + '\'' +
                ", counter=" + counter +
                ", date=" + date +
                '}';
    }
}
