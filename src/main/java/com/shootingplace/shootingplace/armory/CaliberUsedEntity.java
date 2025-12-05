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
public class CaliberUsedEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private String uuid;

    private String belongTo;

    private LocalDate date;
    private LocalTime time;
    private Integer ammoUsed;
    private float unitPrice;

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

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public float getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(float unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getAmmoUsed() {
        return ammoUsed;
    }

    public void setAmmoUsed(Integer ammoUsed) {
        this.ammoUsed = ammoUsed;
    }
}
