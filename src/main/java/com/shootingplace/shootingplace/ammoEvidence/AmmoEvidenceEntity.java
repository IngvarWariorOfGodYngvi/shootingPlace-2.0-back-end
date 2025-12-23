package com.shootingplace.shootingplace.ammoEvidence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.List;

@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AmmoEvidenceEntity {

    @Id
    @UuidGenerator
    private String uuid;

    private LocalDate date;

    private String number;
    @ManyToMany(cascade = CascadeType.ALL)
    private List<AmmoInEvidenceEntity> ammoInEvidenceEntityList;

    private boolean open;

    private boolean forceOpen;
    private boolean locked;


    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setAmmoInEvidenceEntityList(List<AmmoInEvidenceEntity> ammoInEvidenceEntityList) {
        this.ammoInEvidenceEntityList = ammoInEvidenceEntityList;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public void setForceOpen(boolean forceOpen) {
        if (!this.locked) {
            this.forceOpen = forceOpen;
        } else {
            this.forceOpen = false;
        }
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
