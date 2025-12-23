package com.shootingplace.shootingplace.license;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;

@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LicenseEntity {

    @Id
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


    public void setNumber(String number) {
        this.number = number;
    }

    public void setValidThru(LocalDate validThru) {
        this.validThru = validThru;
    }

    public void setPistolPermission(boolean pistolPermission) {
        this.pistolPermission = pistolPermission;
    }

    public void setRiflePermission(boolean riflePermission) {
        this.riflePermission = riflePermission;
    }

    public void setShotgunPermission(boolean shotgunPermission) {
        this.shotgunPermission = shotgunPermission;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public void setCanProlong(boolean canProlong) {
        this.canProlong = canProlong;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }
}
