package com.shootingplace.shootingplace.barCodeCards;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BarCodeCardEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private String uuid;

    private String barCode;

    private boolean isActive;
    /**
     * enter uuid of Member / User
     */
    private String belongsTo;
    /**
     * enter Member / User
     */
    private String type;

    private boolean isMaster;

    private int useCounter;

    private LocalDate activatedDay;

    public LocalDate getActivatedDay() {
        return activatedDay;
    }

    public void setActivatedDay(LocalDate activatedDay) {
        this.activatedDay = activatedDay;
    }

    public void addCountUse() {
        this.useCounter += 1;
    }

    public String getUuid() {
        return uuid;
    }

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getBelongsTo() {
        return belongsTo;
    }

    public boolean isMaster() {
        return isMaster;
    }

    public void setMaster(boolean master) {
        isMaster = master;
    }

    public void setBelongsTo(String belongsTo) {
        this.belongsTo = belongsTo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
