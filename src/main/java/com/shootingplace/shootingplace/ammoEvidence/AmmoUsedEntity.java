package com.shootingplace.shootingplace.ammoEvidence;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AmmoUsedEntity {

    @Id
    @GeneratedValue
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getUuid() {
        return uuid;
    }

    public String getCaliberName() {
        return caliberName;
    }

    public void setCaliberName(String caliberName) {
        this.caliberName = caliberName;
    }

    public String getMemberUUID() {
        return memberUUID;
    }

    public void setMemberUUID(String memberUUID) {
        this.memberUUID = memberUUID;
    }

    public Integer getCounter() {
        return counter;
    }

    public String getCaliberUUID() {
        return caliberUUID;
    }

    public void setCaliberUUID(String caliberUUID) {
        this.caliberUUID = caliberUUID;
    }

    public void setCounter(Integer counter) {
        this.counter = counter;
    }

    public Integer getOtherPersonEntityID() {
        return otherPersonEntityID;
    }

    public void setOtherPersonEntityID(Integer otherPersonEntityID) {
        this.otherPersonEntityID = otherPersonEntityID;
    }

    public String getUserName() {
        return userName;
    }

    public LocalTime getTime() {
        return time;
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
