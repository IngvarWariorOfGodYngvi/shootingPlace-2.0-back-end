package com.shootingplace.shootingplace.shootingPatent;

import jakarta.persistence.Entity;
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
public class ShootingPatentEntity {

    @Id
    @UuidGenerator
    private String uuid;
    private String patentNumber;

    private boolean pistolPermission;

    private boolean riflePermission;

    private boolean shotgunPermission;

    private LocalDate dateOfPosting;

    public String getUuid() {
        return uuid;
    }

    public String getPatentNumber() {
        return patentNumber;
    }

    public void setPatentNumber(String patentNumber) {
        this.patentNumber = patentNumber;
    }

    public Boolean getPistolPermission() {
        return pistolPermission;
    }

    public void setPistolPermission(Boolean pistolPermission) {
        this.pistolPermission = pistolPermission;
    }

    public Boolean getRiflePermission() {
        return riflePermission;
    }

    public void setRiflePermission(Boolean riflePermission) {
        this.riflePermission = riflePermission;
    }

    public Boolean getShotgunPermission() {
        return shotgunPermission;
    }

    public void setShotgunPermission(Boolean shotgunPermission) {
        this.shotgunPermission = shotgunPermission;
    }

    public LocalDate getDateOfPosting() {
        return dateOfPosting;
    }

    public void setDateOfPosting(LocalDate dateOfPosting) {
        this.dateOfPosting = dateOfPosting;
    }

    @Override
    public String toString() {
        return "ShootingPatentEntity{" +
                "uuid='" + uuid + '\'' +
                ", patentNumber='" + patentNumber + '\'' +
                ", pistolPermission=" + pistolPermission +
                ", riflePermission=" + riflePermission +
                ", shotgunPermission=" + shotgunPermission +
                ", dateOfPosting=" + dateOfPosting +
                '}';
    }
}
