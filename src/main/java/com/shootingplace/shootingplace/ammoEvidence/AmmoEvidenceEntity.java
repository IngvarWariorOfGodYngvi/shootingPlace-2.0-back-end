package com.shootingplace.shootingplace.ammoEvidence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AmmoEvidenceEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private String uuid;

    private LocalDate date;

    private String number;
    @ManyToMany(cascade = CascadeType.ALL)
    private List<AmmoInEvidenceEntity> ammoInEvidenceEntityList;

    private boolean open;

    private boolean forceOpen;
    private boolean locked;


    public String getUuid() {
        return uuid;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public List<AmmoInEvidenceEntity> getAmmoInEvidenceEntityList() {
        return ammoInEvidenceEntityList;
    }

    public void setAmmoInEvidenceEntityList(List<AmmoInEvidenceEntity> ammoInEvidenceEntityList) {
        this.ammoInEvidenceEntityList = ammoInEvidenceEntityList;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public boolean isForceOpen() {
        return forceOpen;
    }

    public void setForceOpen(boolean forceOpen) {
        if (!this.locked) {
            this.forceOpen = forceOpen;
        } else {
            this.forceOpen = false;
        }
    }

    public boolean isLocked() {
        return locked;
    }

    /**
     * Użycie tej funkcji sprawi, że Ewidencja zostanie na stałe zamknięta i nie da się już jej otworzyć.
     */
    public void lockEvidence() {
        this.open = false;
        this.forceOpen = false;
        this.locked = true;
    }
}
