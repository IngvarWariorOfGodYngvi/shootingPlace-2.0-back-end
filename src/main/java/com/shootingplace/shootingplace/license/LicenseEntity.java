package com.shootingplace.shootingplace.license;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LicenseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private String uuid;
    @Pattern(regexp = "[0-9]*")
    private String number;
    private LocalDate validThru;
    private boolean pistolPermission;
    private boolean riflePermission;
    private boolean shotgunPermission;
    private boolean valid;
    private boolean canProlong;
    private boolean paid;



    public String getUuid() {
        return uuid;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public LocalDate getValidThru() {
        return validThru;
    }

    public void setValidThru(LocalDate validThru) {
        this.validThru = validThru;
    }

    public boolean isPistolPermission() {
        return pistolPermission;
    }

    public void setPistolPermission(boolean pistolPermission) {
        this.pistolPermission = pistolPermission;
    }

    public boolean isRiflePermission() {
        return riflePermission;
    }

    public void setRiflePermission(boolean riflePermission) {
        this.riflePermission = riflePermission;
    }

    public boolean isShotgunPermission() {
        return shotgunPermission;
    }

    public void setShotgunPermission(boolean shotgunPermission) {
        this.shotgunPermission = shotgunPermission;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public boolean isCanProlong() {
        return canProlong;
    }

    public void setCanProlong(boolean canProlong) {
        this.canProlong = canProlong;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }
}
