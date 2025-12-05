package com.shootingplace.shootingplace.weaponPermission;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WeaponPermissionEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private String uuid;

    private String number;

    private Boolean isExist;

    private String admissionToPossessAWeapon;
    private Boolean admissionToPossessAWeaponIsExist;

    public String getUuid() {
        return uuid;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getAdmissionToPossessAWeapon() {
        return admissionToPossessAWeapon;
    }

    public void setAdmissionToPossessAWeapon(String admissionToPossessAWeapon) {
        this.admissionToPossessAWeapon = admissionToPossessAWeapon;
    }

    public Boolean getExist() {
        return isExist;
    }

    public void setExist(Boolean exist) {
        isExist = exist;
    }

    public Boolean getAdmissionToPossessAWeaponIsExist() {
        return admissionToPossessAWeaponIsExist;
    }

    public void setAdmissionToPossessAWeaponIsExist(Boolean admissionToPossessAWeaponIsExist) {
        this.admissionToPossessAWeaponIsExist = admissionToPossessAWeaponIsExist;
    }
}
