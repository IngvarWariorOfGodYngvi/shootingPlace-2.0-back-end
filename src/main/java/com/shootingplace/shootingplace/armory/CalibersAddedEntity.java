package com.shootingplace.shootingplace.armory;

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
public class CalibersAddedEntity {

    @Id
    @GeneratedValue
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

    public String getUuid() {
        return uuid;
    }

    public String getBelongTo() {
        return belongTo;
    }

    public void setBelongTo(String belongTo) {
        this.belongTo = belongTo;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Integer getAmmoAdded() {
        return ammoAdded;
    }

    public void setAmmoAdded(Integer ammoAdded) {
        this.ammoAdded = ammoAdded;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStateForAddedDay() {
        return stateForAddedDay;
    }

    public void setStateForAddedDay(Integer stateForAddedDay) {
        this.stateForAddedDay = stateForAddedDay;
    }

    public Integer getFinalStateForAddedDay() {
        return finalStateForAddedDay;
    }

    public void setFinalStateForAddedDay(Integer finalStateForAddedDay) {
        this.finalStateForAddedDay = finalStateForAddedDay;
    }

    public String getCaliberName() {
        return caliberName;
    }

    public void setCaliberName(String caliberName) {
        this.caliberName = caliberName;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

    public String getImageUUID() {
        return imageUUID;
    }

    public void setImageUUID(String imageUUID) {
        this.imageUUID = imageUUID;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }
}
