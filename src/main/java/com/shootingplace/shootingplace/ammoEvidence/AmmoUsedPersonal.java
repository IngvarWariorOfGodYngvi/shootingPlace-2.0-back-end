package com.shootingplace.shootingplace.ammoEvidence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AmmoUsedPersonal {
    private String caliberName;

    private String memberUUID;

    private String caliberUUID;

    private Integer otherPersonEntityID;

    private String memberName;

    private Integer counter;

    private LocalDate date;

    private LocalTime time;

    public LocalDate getDate() {
        return date;
    }

    public void setCaliberName(String caliberName) {
        this.caliberName = caliberName;
    }

    public void setMemberUUID(String memberUUID) {
        this.memberUUID = memberUUID;
    }

    public void setCounter(Integer counter) {
        this.counter = counter;
    }

    public void setCaliberUUID(String caliberUUID) {
        this.caliberUUID = caliberUUID;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }
}
