package com.shootingplace.shootingplace.armory;

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
public class CalibersAddedEntity {

    @Id
    @UuidGenerator
    private String uuid;

    private String belongTo;
    private String caliberName;
    private String description;

    private LocalDate date;
    private LocalTime time;
    private Integer ammoAdded;
    private String imageUUID;

    private Integer stateForAddedDay;
    private Integer finalStateForAddedDay;
    private String addedBy;

    public void setBelongTo(String belongTo) {
        this.belongTo = belongTo;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setAmmoAdded(Integer ammoAdded) {
        this.ammoAdded = ammoAdded;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStateForAddedDay(Integer stateForAddedDay) {
        this.stateForAddedDay = stateForAddedDay;
    }

    public void setFinalStateForAddedDay(Integer finalStateForAddedDay) {
        this.finalStateForAddedDay = finalStateForAddedDay;
    }

    public void setCaliberName(String caliberName) {
        this.caliberName = caliberName;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

    public void setImageUUID(String imageUUID) {
        this.imageUUID = imageUUID;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }
}
