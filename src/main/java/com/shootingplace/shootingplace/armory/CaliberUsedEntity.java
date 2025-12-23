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
public class CaliberUsedEntity {

    @Id
    @UuidGenerator
    private String uuid;

    private String belongTo;

    private LocalDate date;
    private LocalTime time;
    private Integer ammoUsed;
    private float unitPrice;

    public void setBelongTo(String belongTo) {
        this.belongTo = belongTo;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public void setUnitPrice(float unitPrice) {
        this.unitPrice = unitPrice;
    }

    public void setAmmoUsed(Integer ammoUsed) {
        this.ammoUsed = ammoUsed;
    }
}
